package io.github.mangomaner.mangobot.module.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Workspace / 全局能力目录管理器
 *
 * <p>工作区按 (Bot, 来源) 拆分，来源默认人格即该工作区的 AGENTS.md（活文件，随改动热生效）：
 * <ul>
 *   <li>Web 端：data/workspaces/&lt;botId&gt;/（botId 为空时 sanitize 为 default），AGENTS.md 为 Web 人格</li>
 *   <li>群聊：data/workspaces/&lt;botId&gt;/group/，AGENTS.md 为群聊默认人格</li>
 *   <li>私聊：data/workspaces/&lt;botId&gt;/private/，AGENTS.md 为私聊默认人格</li>
 * </ul>
 * 每个会话（&lt;userId&gt;）默认没有独立 AGENTS.md，直接回退到所在工作区的默认人格；
 * 只有定制人格的会话才在 &lt;工作区&gt;/&lt;userId&gt;/AGENTS.md 写入定制内容，恢复默认时删除该文件。
 * 因此默认人格文件改动后，所有使用默认的会话会立即跟随，不会残留旧快照。
 *
 * <p>全局能力目录：data/capabilities/（skills 内容 + tools.json 白名单，所有 Bot 共用）；
 * AgentState 存储：data/state/。能力定义（工具/MCP/Skill 元数据）以 DB 为 source of truth。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentWorkspaceManager {

    private static final String WORKSPACES_DIR = "data/workspaces";
    private static final String CAPABILITIES_DIR = "data/capabilities";
    private static final String SKILLS_DIR = "data/capabilities/skills";
    private static final String STATE_DIR = "data/state";
    private static final String TOOLS_JSON = "tools.json";

    /** 升级前技能内容所在目录，首次启动迁移到全局能力目录 */
    private static final String LEGACY_SKILLS_DIR = "data/skills";

    private static final String AGENTS_TEMPLATE_RESOURCE = "agentscope/AGENTS.template.md";
    private static final String AGENTS_GROUP_TEMPLATE_RESOURCE = "agentscope/AGENTS_GROUP.template.md";
    private static final String AGENTS_PRIVATE_TEMPLATE_RESOURCE = "agentscope/AGENTS_PRIVATE.template.md";

    /** persona 模板版本标记：旧工作区（无此文件）首次启动会重播种 AGENTS.md */
    private static final String PERSONA_VERSION_FILE = ".persona-version";
    private static final String PERSONA_VERSION = "2";

    /** 旧版 IM 兜底模板的特征串，用于识别并替换为 Web 人格 */
    private static final String LEGACY_BASE_MARKER = "兜底模板";

    /** Web 端工作区名（botId 为空时 sanitize 为 default） */
    private static final String WEB_WORKSPACE = "default";

    private static final String GROUP_WORKSPACE = "group";
    private static final String PRIVATE_WORKSPACE = "private";

    private final ObjectMapper objectMapper;

    /**
     * 启动时初始化：确保全局能力目录存在、迁移旧技能目录、生成默认 tools.json 白名单。
     * 由 MangoBotApplication.main 显式调用（Bean 内禁止 @PostConstruct 业务初始化）。
     */
    public void init() {
        FileUtils.createDirectory(getCapabilitiesDir());
        Path skillsDir = getSkillsDirectory();
        migrateLegacySkills(skillsDir);
        FileUtils.createDirectory(skillsDir);
        FileUtils.createDirectory(getStateDir());
        seedToolsJson();
        // 为所有已存在的 Bot 工作区做旧 IM 布局迁移 + 兜底播种 AGENTS.md（缺失时不播种，
        // overlay 会兜底注入 project 根目录的 AGENTS.md——即项目开发文档，极大浪费 token）
        for (String botId : listBotWorkspaces()) {
            migrateLegacySourceWorkspace(resolveBotWorkspace(botId));
            ensureBotWorkspace(botId);
        }
        log.info("Agent workspace initialized: capabilities={}, skills={}, state={}",
                getCapabilitiesDir(), skillsDir, getStateDir());
    }

    /** 全局能力目录（DB 定义之外的技能内容文件、tools.json 白名单） */
    public Path getCapabilitiesDir() {
        return FileUtils.resolvePath(CAPABILITIES_DIR);
    }

    /** 全局技能内容目录 */
    public Path getSkillsDirectory() {
        return FileUtils.resolvePath(SKILLS_DIR);
    }

    /** AgentState 本地存储目录 */
    public Path getStateDir() {
        return FileUtils.resolvePath(STATE_DIR);
    }

    /** 解析 Bot 工作区根目录（不创建） */
    public Path resolveBotWorkspace(String botId) {
        return FileUtils.resolvePath(WORKSPACES_DIR).resolve(sanitize(botId));
    }

    /** 解析 (Bot, 来源) 的 Agent 工作区（不创建）：group/private 为根目录下子工作区，Web 即根目录 */
    public Path resolveAgentWorkspace(String botId, SessionSource source) {
        Path root = resolveBotWorkspace(botId);
        SessionSource src = source != null ? source : SessionSource.WEB;
        if (src == SessionSource.GROUP) {
            return root.resolve(GROUP_WORKSPACE);
        }
        if (src == SessionSource.PRIVATE) {
            return root.resolve(PRIVATE_WORKSPACE);
        }
        return root;
    }

    /**
     * 确保指定 (Bot, 来源) 的 Agent 工作区存在并播种对应来源默认人格，返回工作区路径。
     * <ul>
     *   <li>Web：播种 Web 人格 AGENTS.md</li>
     *   <li>群聊/私聊：确保根工作区 + 对应子工作区 AGENTS.md（来源默认人格）</li>
     * </ul>
     */
    public Path ensureAgentWorkspace(String botId, SessionSource source) {
        SessionSource src = source != null ? source : SessionSource.WEB;
        ensureBotWorkspace(botId);
        if (src == SessionSource.GROUP || src == SessionSource.PRIVATE) {
            ensureSourceWorkspace(botId, src);
        }
        return resolveAgentWorkspace(botId, src);
    }

    /**
     * 确保 Bot 工作区根目录存在并播种 Web 人格 AGENTS.md；非 Web 端 Bot 顺带确保群聊/私聊子工作区。
     */
    public Path ensureBotWorkspace(String botId) {
        Path workspace = resolveBotWorkspace(botId);
        FileUtils.createDirectory(workspace);
        seedAgentsMd(workspace);
        if (!isWebWorkspace(botId)) {
            ensureSourceWorkspace(botId, SessionSource.GROUP);
            ensureSourceWorkspace(botId, SessionSource.PRIVATE);
        }
        return workspace;
    }

    /** 列出所有已初始化的 Bot 工作区（仅根目录，不含 group/private 子工作区） */
    public List<String> listBotWorkspaces() {
        List<String> result = new ArrayList<>();
        Path root = FileUtils.resolvePath(WORKSPACES_DIR);
        FileUtils.createDirectory(root);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    result.add(child.getFileName().toString());
                }
            }
        } catch (IOException e) {
            log.error("Failed to list bot workspaces", e);
        }
        return result;
    }

    /**
     * 读取某来源的默认人格内容（即该来源工作区的 AGENTS.md），文件缺失时回退到 classpath 模板。
     */
    public String readDefaultPersona(String botId, SessionSource source) {
        SessionSource src = source != null ? source : SessionSource.WEB;
        Path ws = resolveAgentWorkspace(botId, src);
        return readPersonaFile(ws.resolve("AGENTS.md"), templateFor(src));
    }

    /**
     * 写入会话级 AGENTS.md（仅定制人格时调用）；content 为空时等同于删除。
     * 写入后 &lt;工作区&gt;/&lt;userId&gt;/AGENTS.md 覆盖该来源默认人格。
     */
    public void writeSessionAgentsMdIfChanged(String botId, SessionSource source, String chatId, String content) {
        if (content == null || content.isBlank()) {
            deleteSessionAgentsMd(botId, source, chatId);
            return;
        }
        String userId = AgentPathKeys.userId(botId, source, chatId);
        Path file = resolveAgentWorkspace(botId, source).resolve(userId).resolve("AGENTS.md");
        try {
            if (Files.isRegularFile(file)) {
                String existing = Files.readString(file, StandardCharsets.UTF_8);
                if (content.equals(existing)) {
                    return;
                }
            }
            FileUtils.createDirectory(file.getParent());
            FileUtils.writeString(file, content);
            log.info("Session persona materialized: {}", file);
        } catch (IOException e) {
            log.error("Failed to materialize session persona: {}", file, e);
        }
    }

    /**
     * 删除会话级 AGENTS.md：恢复默认后回退到所在工作区的来源默认人格（随默认文件改动热生效）。
     */
    public void deleteSessionAgentsMd(String botId, SessionSource source, String chatId) {
        String userId = AgentPathKeys.userId(botId, source, chatId);
        Path file = resolveAgentWorkspace(botId, source).resolve(userId).resolve("AGENTS.md");
        try {
            if (Files.deleteIfExists(file)) {
                log.info("Session persona removed (restore default): {}", file);
            }
        } catch (IOException e) {
            log.error("Failed to delete session persona: {}", file, e);
        }
    }

    /** 全局调用白名单（allow/deny，作用于所有已注册工具，含 harness 内置工具） */
    public ToolsWhitelist loadToolsWhitelist() {
        Path file = getCapabilitiesDir().resolve(TOOLS_JSON);
        if (!Files.exists(file)) {
            return ToolsWhitelist.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(file.toFile());
            List<String> allow = readStringArray(node.get("allow"));
            List<String> deny = readStringArray(node.get("deny"));
            return new ToolsWhitelist(allow, deny);
        } catch (IOException e) {
            log.error("Failed to read tools whitelist: {}", file, e);
            return ToolsWhitelist.empty();
        }
    }

    /**
     * 旧 IM 布局迁移（一次性，幂等）：AGENTS_GROUP.md / AGENTS_PRIVATE.md 迁为 group/private
     * 子工作区的 AGENTS.md；根目录下 bot_&lt;botId&gt;_group_* / _private_* 会话目录迁入对应子工作区。
     */
    private void migrateLegacySourceWorkspace(Path botWs) {
        try {
            Path groupDir = botWs.resolve(GROUP_WORKSPACE);
            Path privateDir = botWs.resolve(PRIVATE_WORKSPACE);
            boolean migrated = false;
            migrated |= moveLegacyDefaultFile(botWs.resolve("AGENTS_GROUP.md"), groupDir);
            migrated |= moveLegacyDefaultFile(botWs.resolve("AGENTS_PRIVATE.md"), privateDir);

            // 先收集再移动，避免在 DirectoryStream 迭代期间改动目录
            List<Path> sessionDirs = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(botWs)) {
                for (Path child : stream) {
                    String name = child.getFileName().toString();
                    if (!Files.isDirectory(child) || !name.startsWith("bot_")) {
                        continue;
                    }
                    if (name.contains("_group_") || name.contains("_private_")) {
                        sessionDirs.add(child);
                    }
                }
            }
            for (Path child : sessionDirs) {
                String name = child.getFileName().toString();
                Path targetDir = name.contains("_group_") ? groupDir : privateDir;
                FileUtils.createDirectory(targetDir);
                Files.move(child, targetDir.resolve(name));
                migrated = true;
            }
            if (migrated) {
                log.info("Migrated legacy IM workspace layout: {}", botWs);
            }
        } catch (IOException e) {
            log.error("Failed to migrate legacy workspace: {}", botWs, e);
        }
    }

    /** 迁移旧版来源默认人格文件到子工作区 AGENTS.md；目标已存在时丢弃旧文件。 */
    private boolean moveLegacyDefaultFile(Path source, Path targetDir) throws IOException {
        if (!Files.exists(source)) {
            return false;
        }
        FileUtils.createDirectory(targetDir);
        Path target = targetDir.resolve("AGENTS.md");
        if (Files.exists(target)) {
            Files.deleteIfExists(source);
        } else {
            Files.move(source, target);
            // 写版本标记，避免 ensureSourceWorkspace 因版本缺失用模板覆盖用户默认人格
            FileUtils.writeString(targetDir.resolve(PERSONA_VERSION_FILE), PERSONA_VERSION);
        }
        return true;
    }

    /** 将 botId 规整为文件系统安全段 */
    private static String sanitize(String botId) {
        if (!StringUtils.hasText(botId)) {
            return "default";
        }
        return botId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** Web 端：botId 为空，或工作区名恰为 default（init 迁移存量工作区时传入的是目录名） */
    private static boolean isWebWorkspace(String botId) {
        return !StringUtils.hasText(botId) || WEB_WORKSPACE.equalsIgnoreCase(botId);
    }

    /**
     * 播种根目录 AGENTS.md（Web 人格）：已存在且带 persona 版本标记则不覆盖（保留用户手改）；
     * 旧版 IM 兜底模板（无实际人格）替换为 Web 人格。
     */
    private void seedAgentsMd(Path workspace) {
        Path agentsMd = workspace.resolve("AGENTS.md");
        Path versionFile = workspace.resolve(PERSONA_VERSION_FILE);
        if (Files.exists(agentsMd) && Files.exists(versionFile)) {
            if (isLegacyBasePlaceholder(agentsMd)) {
                replaceAgentsMd(agentsMd, versionFile, AGENTS_TEMPLATE_RESOURCE);
                log.info("Replaced legacy base placeholder AGENTS.md with web persona: {}", workspace);
            }
            return;
        }
        replaceAgentsMd(agentsMd, versionFile, AGENTS_TEMPLATE_RESOURCE);
        log.info("Seeded web AGENTS.md for workspace: {}", workspace);
    }

    /** 播种来源默认人格：子工作区 AGENTS.md 仅缺失时写入（迁移/用户手改的文件不会被覆盖） */
    private Path ensureSourceWorkspace(String botId, SessionSource source) {
        Path ws = resolveAgentWorkspace(botId, source);
        FileUtils.createDirectory(ws);
        Path agentsMd = ws.resolve("AGENTS.md");
        if (!Files.exists(agentsMd)) {
            String template = readResource(templateFor(source));
            if (template != null) {
                FileUtils.writeString(agentsMd, template);
                log.info("Seeded {} AGENTS.md for workspace: {}", source.getSourceKey(), ws);
            }
        }
        return ws;
    }

    private void replaceAgentsMd(Path agentsMd, Path versionFile, String resource) {
        String template = readResource(resource);
        if (template != null) {
            FileUtils.writeString(agentsMd, template);
        }
        FileUtils.writeString(versionFile, PERSONA_VERSION);
    }

    private static String templateFor(SessionSource source) {
        if (source == SessionSource.GROUP) {
            return AGENTS_GROUP_TEMPLATE_RESOURCE;
        }
        if (source == SessionSource.PRIVATE) {
            return AGENTS_PRIVATE_TEMPLATE_RESOURCE;
        }
        return AGENTS_TEMPLATE_RESOURCE;
    }

    private boolean isLegacyBasePlaceholder(Path agentsMd) {
        try {
            return Files.readString(agentsMd, StandardCharsets.UTF_8).contains(LEGACY_BASE_MARKER);
        } catch (IOException e) {
            return false;
        }
    }

    private String readPersonaFile(Path file, String fallbackResource) {
        if (Files.isRegularFile(file)) {
            try {
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Failed to read persona file: {}", file, e);
            }
        }
        return readResource(fallbackResource);
    }

    private String readResource(String resourceName) {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (in == null) {
                log.warn("Template not found: {}", resourceName);
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read template: {}", resourceName, e);
            return null;
        }
    }

    private void seedToolsJson() {
        Path file = getCapabilitiesDir().resolve(TOOLS_JSON);
        if (Files.exists(file)) {
            return;
        }
        String defaultContent = """
                {
                  // 全局工具白名单（所有 Bot 一致）：allow 在全部工具注册后应用，非空时只暴露列出的工具
                  // 默认只保留聊天/工具/技能所需的最小集合，减少每次调用的工具 schema token 开销
                  "allow": [
                    "sendTextMessage",
                    "sendPrivateTextMessage",
                    "getMemeImages",
                    "sendMemeImage",
                    "getCurrentDateTime",
                    "getCurrentDate",
                    "getCurrentTime",
                    "formatDateTime",
                    "add",
                    "subtract",
                    "multiply",
                    "divide",
                    "power",
                    "sqrt",
                    "percentage",
                    "read_file",
                    "grep_files",
                    "glob_files",
                    "list_files",
                    "load_skill_through_path",
                    "memory_search",
                    "memory_get",
                    "memory_save"
                  ],
                  // deny 优先级高于 allow；危险工具一律不暴露
                  "deny": ["execute", "write_file", "edit_file"]
                }
                """;
        FileUtils.writeString(file, defaultContent);
        log.info("Seeded default tools whitelist: {}", file);
    }

    private void migrateLegacySkills(Path targetSkillsDir) {
        Path legacy = FileUtils.resolvePath(LEGACY_SKILLS_DIR);
        if (!Files.isDirectory(legacy) || Files.exists(targetSkillsDir)) {
            return;
        }
        try {
            Files.createDirectories(targetSkillsDir);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(legacy)) {
                for (Path child : stream) {
                    copyRecursively(child, targetSkillsDir.resolve(child.getFileName().toString()));
                }
            }
            log.info("Migrated legacy skills from {} to {}", legacy, targetSkillsDir);
        } catch (IOException e) {
            log.error("Failed to migrate legacy skills", e);
        }
    }

    private void copyRecursively(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path child : stream) {
                    copyRecursively(child, target.resolve(child.getFileName().toString()));
                }
            }
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static List<String> readStringArray(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> {
                if (item.isTextual()) {
                    result.add(item.asText());
                }
            });
        }
        return result;
    }

    /** 工具白名单 */
    public record ToolsWhitelist(List<String> allow, List<String> deny) {
        public static ToolsWhitelist empty() {
            return new ToolsWhitelist(List.of(), List.of());
        }

        public boolean isEmpty() {
            return allow.isEmpty() && deny.isEmpty();
        }
    }
}
