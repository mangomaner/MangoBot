package io.github.mangomaner.mangobot.module.agent.capability.skill;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.skill.AgentSkill;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.service.AgentSkillConfigService;
import io.github.mangomaner.mangobot.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Skill 管理器
 * 
 * <p>负责管理 Agent Skill 的生命周期：
 * <ul>
 *   <li>{@link #init()} - 启动时扫描并同步 Skill 目录</li>
 *   <li>{@link #loadSkill(AgentSkillConfig)} - 加载 Skill 到 Agent</li>
 *   <li>{@link #createSkillDirectory(String)} - 创建 Skill 目录</li>
 *   <li>{@link #writeSkillContent(String, String)} - 写入 Skill 内容</li>
 *   <li>{@link #deleteSkillDirectory(String)} - 删除 Skill 目录</li>
 * </ul>
 * 
 * <h3>Skill 目录结构</h3>
 * <pre>
 * data/skills/
 * └── my-skill/
 *     ├── SKILL.md           # Skill 主文件（必须）
 *     ├── references/        # 参考文档（可选）
 *     │   └── doc.md
 *     ├── examples/          # 示例文件（可选）
 *     │   └── example.md
 *     └── scripts/           # 脚本文件（可选）
 *         └── helper.sh
 * </pre>
 * 
 * <h3>SKILL.md 格式</h3>
 * <pre>
 * ---
 * name: My Skill
 * description: A sample skill
 * bound_tool_ids: [1, 2, 3]
 * ---
 * 
 * Skill content here...
 * </pre>
 * 
 * @see AgentSkill
 * @see AgentSkillConfig
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SkillManager {

    /** Skill 存储目录 */
    private static final String SKILLS_DIR = "data/skills";
    
    private final AgentSkillConfigService skillConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 Skill 存储目录路径
     * 
     * @return Skill 目录路径
     */
    public Path getSkillsDirectory() {
        return FileUtils.resolvePath(SKILLS_DIR);
    }

    /**
     * 初始化 Skill 同步
     * 
     * <p>扫描 Skill 目录，将文件系统中的 Skill 同步到数据库：
     * <ul>
     *   <li>新增的 Skill：写入数据库，默认禁用</li>
     *   <li>已存在的 Skill：更新元数据，保留启用状态</li>
     *   <li>数据库中存在但文件不存在的 Skill：保留配置（标记为不可用）</li>
     * </ul>
     */
    public void init() {
        Path skillsDir = getSkillsDirectory();
        FileUtils.createDirectory(skillsDir);
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(skillsDir)) {
            for (Path skillPath : stream) {
                if (Files.isDirectory(skillPath)) {
                    Path skillMdPath = skillPath.resolve("SKILL.md");
                    if (Files.exists(skillMdPath)) {
                        String skillPathName = skillPath.getFileName().toString();
                        syncSkill(skillPathName, skillMdPath);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan skills directory", e);
        }
    }

    /**
     * 同步单个 Skill 到数据库
     */
    private void syncSkill(String skillPathName, Path skillMdPath) {
        try {
            String content = Files.readString(skillMdPath);
            SkillMetadata metadata = parseSkillMetadata(content);
            
            AgentSkillConfig config = skillConfigService.getBySkillPath(skillPathName);
            
            if (config == null) {
                config = new AgentSkillConfig();
                config.setSkillPath(skillPathName);
                config.setEnabled(false);
                // 默认支持所有来源
                String allSourcesJson = buildAllSourcesJson();
                config.setEnabledList(allSourcesJson);
                config.setAvailableList(allSourcesJson);
            }

            config.setSkillName(metadata.name);
            config.setDescription(metadata.description);
            if (metadata.boundToolIds != null && !metadata.boundToolIds.isEmpty()) {
                config.setBoundToolIds(objectMapper.writeValueAsString(metadata.boundToolIds));
            }
            
            skillConfigService.saveOrUpdate(config);
            log.info("Skill synced: {}", skillPathName);
            
        } catch (IOException e) {
            log.error("Failed to read skill file: {}", skillMdPath, e);
        }
    }

    /**
     * 加载 Skill
     * 
     * <p>从文件系统加载 Skill 内容，包括：
     * <ul>
     *   <li>SKILL.md 主文件内容</li>
     *   <li>references/ 目录下的参考文档</li>
     *   <li>examples/ 目录下的示例文件</li>
     *   <li>scripts/ 目录下的脚本文件</li>
     * </ul>
     * 
     * @param config Skill 配置
     * @return AgentSkill 实例（加载失败返回 empty）
     */
    public Optional<AgentSkill> loadSkill(AgentSkillConfig config) {
        Path skillDir = getSkillsDirectory().resolve(config.getSkillPath());
        Path skillMdPath = skillDir.resolve("SKILL.md");
        
        if (!Files.exists(skillMdPath)) {
            log.warn("Skill file not found: {}", skillMdPath);
            return Optional.empty();
        }
        
        try {
            String content = Files.readString(skillMdPath);
            Map<String, String> resources = loadResources(skillDir);
            
            AgentSkill skill = AgentSkill.builder()
                .name(config.getSkillName())
                .description(config.getDescription())
                .skillContent(content)
                .resources(resources)
                .build();
            
            return Optional.of(skill);
        } catch (IOException e) {
            log.error("Failed to load skill: {}", config.getSkillPath(), e);
            return Optional.empty();
        }
    }

    /**
     * 加载 Skill 资源文件
     */
    private Map<String, String> loadResources(Path skillDir) throws IOException {
        Map<String, String> resources = new HashMap<>();
        String[] resourceDirs = {"references", "examples", "scripts"};
        
        for (String dirName : resourceDirs) {
            Path dir = skillDir.resolve(dirName);
            if (Files.exists(dir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path file : stream) {
                        if (Files.isRegularFile(file)) {
                            String relativePath = dirName + "/" + file.getFileName();
                            resources.put(relativePath, Files.readString(file));
                        }
                    }
                }
            }
        }
        return resources;
    }

    /**
     * 解析 SKILL.md 的 YAML frontmatter
     */
    @SuppressWarnings("unchecked")
    private SkillMetadata parseSkillMetadata(String content) {
        SkillMetadata metadata = new SkillMetadata();
        if (content.startsWith("---")) {
            int endIndex = content.indexOf("---", 3);
            if (endIndex > 0) {
                String frontmatter = content.substring(3, endIndex);
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(frontmatter);
                Object nameObj = data.get("name");
                if (nameObj != null) {
                    metadata.name = String.valueOf(nameObj);
                }
                Object descObj = data.get("description");
                if (descObj != null) {
                    metadata.description = String.valueOf(descObj);
                }
                Object boundToolsObj = data.get("bound_tool_ids");
                if (boundToolsObj instanceof List) {
                    List<?> list = (List<?>) boundToolsObj;
                    metadata.boundToolIds = list.stream()
                            .map(obj -> {
                                if (obj instanceof Number) {
                                    return ((Number) obj).intValue();
                                }
                                return Integer.parseInt(String.valueOf(obj));
                            })
                            .collect(Collectors.toList());
                }
            }
        }
        return metadata;
    }

    /**
     * 创建 Skill 目录
     * 
     * @param skillPath Skill 路径（相对于 skills 目录）
     */
    public void createSkillDirectory(String skillPath) {
        Path skillDir = getSkillsDirectory().resolve(skillPath);
        FileUtils.createDirectory(skillDir);
    }

    /**
     * 写入 Skill 内容
     * 
     * @param skillPath Skill 路径
     * @param content SKILL.md 内容
     */
    public void writeSkillContent(String skillPath, String content) throws IOException {
        Path skillMdPath = getSkillsDirectory().resolve(skillPath).resolve("SKILL.md");
        FileUtils.createParentDirectories(skillMdPath);
        Files.writeString(skillMdPath, content);
    }

    /**
     * 读取 Skill 内容
     * 
     * @param skillPath Skill 路径
     * @return SKILL.md 内容（不存在返回 null）
     */
    public String readSkillContent(String skillPath) throws IOException {
        Path skillMdPath = getSkillsDirectory().resolve(skillPath).resolve("SKILL.md");
        if (!Files.exists(skillMdPath)) {
            return null;
        }
        return Files.readString(skillMdPath);
    }

    /**
     * 删除 Skill 目录
     * 
     * @param skillPath Skill 路径
     */
    public void deleteSkillDirectory(String skillPath) throws IOException {
        Path skillDir = getSkillsDirectory().resolve(skillPath);
        if (Files.exists(skillDir)) {
            deleteRecursively(skillDir);
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path child : stream) {
                    deleteRecursively(child);
                }
            }
        }
        Files.delete(path);
    }

    /** Skill 元数据 */
    private static class SkillMetadata {
        String name;
        String description;
        List<Integer> boundToolIds;
    }

    /**
     * 构建包含所有来源的 JSON 字符串
     *
     * @return 所有 SessionSource 的 JSON 数组字符串
     */
    private String buildAllSourcesJson() {
        List<String> allSourceKeys = Arrays.stream(SessionSource.values())
                .map(SessionSource::getSourceKey)
                .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(allSourceKeys);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize all sources, using hardcoded default", e);
            return "[\"web\",\"group\",\"private\"]";
        }
    }
}
