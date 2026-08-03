package io.github.mangomaner.mangobot.module.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * <ul>
 *   <li>每 Bot 一个工作区：data/workspaces/&lt;botId&gt;/（人格、知识、运行时数据）</li>
 *   <li>全局能力目录：data/capabilities/（skills 内容 + tools.json 白名单，所有 Bot 共用）</li>
 *   <li>AgentState 存储：data/state/</li>
 * </ul>
 *
 * <p>能力定义（工具/MCP/Skill 元数据）以 DB 为 source of truth，workspace 只承载
 * 白名单与人格/知识内容。
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
        // 为所有已存在的 Bot 工作区兜底播种 AGENTS.md（缺失时若不播种，
        // overlay 会兜底注入 project 根目录的 AGENTS.md——即项目开发文档，极大浪费 token）
        for (String botId : listBotWorkspaces()) {
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

    /** 解析 Bot 工作区路径（不创建） */
    public Path resolveBotWorkspace(String botId) {
        return FileUtils.resolvePath(WORKSPACES_DIR).resolve(sanitize(botId));
    }

    /** 确保 Bot 工作区存在并写入 AGENTS.md 模板，返回工作区路径 */
    public Path ensureBotWorkspace(String botId) {
        Path workspace = resolveBotWorkspace(botId);
        FileUtils.createDirectory(workspace);
        seedAgentsMd(workspace);
        return workspace;
    }

    /** 列出所有已初始化的 Bot 工作区 */
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

    /** 将 botId 规整为文件系统安全段 */
    private static String sanitize(String botId) {
        if (!StringUtils.hasText(botId)) {
            return "default";
        }
        return botId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private void seedAgentsMd(Path workspace) {
        Path agentsMd = workspace.resolve("AGENTS.md");
        if (Files.exists(agentsMd)) {
            return;
        }
        String template = readTemplate();
        if (template != null) {
            FileUtils.writeString(agentsMd, template);
            log.info("Seeded AGENTS.md for workspace: {}", workspace);
        }
    }

    private String readTemplate() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(AGENTS_TEMPLATE_RESOURCE)) {
            if (in == null) {
                log.warn("AGENTS template not found: {}", AGENTS_TEMPLATE_RESOURCE);
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read AGENTS template", e);
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
