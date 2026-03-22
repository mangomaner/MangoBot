package io.github.mangomaner.mangobot.module.agent.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.module.agent.capability.mcp.McpConnectionManager;
import io.github.mangomaner.mangobot.module.agent.capability.skill.SkillManager;
import io.github.mangomaner.mangobot.module.agent.capability.tool.JavaToolLoader;
import io.github.mangomaner.mangobot.module.agent.hook.StreamingToolHook;
import io.github.mangomaner.mangobot.module.agent.manager.MemoryManager;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import io.github.mangomaner.mangobot.api.enums.ModelRole;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.github.mangomaner.mangobot.system.exception.BusinessException;
import io.github.mangomaner.mangobot.module.agent.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent 工厂
 * 
 * <p>负责创建和配置 ReActAgent 实例：
 * <ul>
 *   <li>加载已启用的 Java 工具</li>
 *   <li>加载已启用的 MCP 工具</li>
 *   <li>加载已启用的 Skill</li>
 * </ul>
 * <p>
 * 工具和 Skill 会根据当前会话来源进行筛选，只加载 enabledList 包含该来源的项。
 * 
 * @see JavaToolLoader
 * @see McpConnectionManager
 * @see SkillManager
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final MemoryManager memoryManager;
    private final StreamingToolHook streamingToolHook;
    
    private final AgentJavaToolConfigService javaToolConfigService;
    private final AgentMcpConfigService mcpConfigService;
    private final AgentMcpToolConfigService mcpToolConfigService;
    private final AgentSkillConfigService skillConfigService;
    private final ChatSessionService chatSessionService;
    
    private final JavaToolLoader javaToolLoader;
    private final McpConnectionManager mcpConnectionManager;
    private final SkillManager skillManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建 Agent
     * 
     * @param sessionId 会话 ID
     * @return ReActAgent 实例
     */
    public ReActAgent createAgent(Integer sessionId) {
        return createAgentWithPrompt(sessionId, "");
    }
    public ReActAgent createAgentWithPrompt(Integer sessionId, String prompt) {
        String agentName = "MangoBot-" + sessionId;
        log.info("Creating ReActAgent for session: {}, name: {}", sessionId, agentName);

        ChatSessionVO chatSession = chatSessionService.getSessionById(sessionId);
        SessionSource sessionSource = chatSession.getSource();
        if (sessionSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话来源不能为空");
        }

        AutoContextMemory memory = memoryManager.getOrCreateMemory(sessionId);
        Toolkit toolkit = new Toolkit();
        SkillBox skillBox = new SkillBox(toolkit);

        loadJavaTools(toolkit, sessionSource);
        loadMcpTools(toolkit, sessionSource);
        buildSkillBox(skillBox, toolkit, sessionSource);

        // 添加上下文信息，用于工具参数
        ToolExecutionContext context = ToolExecutionContext.builder()
                .register(ChatContext.builder()
                        .sessionId(sessionId)
                        .botId(chatSession.getBotId())
                        .chatId(chatSession.getChatId())
                        .toolExecuteState(new ToolExecuteState())
                        .build()
                )
                .build();

        OpenAIChatModel model = MangoModelApi.getModel(ModelRole.MAIN);

        if (sessionSource == SessionSource.GROUP) {
            return ReActAgent.builder()
                    .name(agentName)
                    .sysPrompt(prompt)
                    .model(model)
                    .toolkit(toolkit)
                    .toolExecutionContext(context)
                    .skillBox(skillBox.getAllSkillIds().isEmpty() ? null : skillBox)
                    .enableMetaTool(true)
                    .hooks(List.of(streamingToolHook))
                    .build();
        }
        return ReActAgent.builder()
                .name(agentName)
                .sysPrompt(prompt)
                .model(model)
                .memory(memory)
                .toolkit(toolkit)
                .toolExecutionContext(context)
                .skillBox(skillBox.getAllSkillIds().isEmpty() ? null : skillBox)
                .enableMetaTool(true)
                .hooks(List.of(streamingToolHook))
                .build();
    }

    // —————————————————————— 下方都是辅助工具 ——————————————————————//

    /**
     * 加载 Java 工具
     * 
     * @param toolkit 工具箱
     * @param sessionSource 会话来源
     */
    private void loadJavaTools(Toolkit toolkit, SessionSource sessionSource) {
        List<AgentJavaToolConfig> enabledTools = javaToolConfigService.listEnabled();
        
        for (AgentJavaToolConfig config : enabledTools) {
            // 检查 enabledList 是否包含当前会话来源
            if (!isSourceEnabled(config.getEnabledList(), sessionSource)) {
                log.debug("Java tool {} not enabled for source: {}", config.getClassName(), sessionSource);
                continue;
            }
            
            javaToolLoader.loadTool(config).ifPresent(tool -> {
                toolkit.registerTool(tool);
                log.debug("Java tool loaded: {} for source: {}", config.getClassName(), sessionSource);
            });
        }
    }

    /**
     * 加载 MCP 工具
     * 
     * @param toolkit 工具箱
     * @param sessionSource 会话来源
     */
    private void loadMcpTools(Toolkit toolkit, SessionSource sessionSource) {
        List<AgentMcpConfig> enabledMcps = mcpConfigService.listEnabled();
        Map<Integer, McpClientWrapper> mcpClients = mcpConnectionManager.getAllClients();
        
        for (AgentMcpConfig mcpConfig : enabledMcps) {
            McpClientWrapper client = mcpClients.get(mcpConfig.getId());
            
            if (client == null) {
                log.warn("MCP client not initialized for config: {} (ID: {}). Skipping tool registration.", 
                    mcpConfig.getMcpName(), mcpConfig.getId());
                continue;
            }
            
            try {
                // 获取该 MCP 下所有启用的工具配置
                List<AgentMcpToolConfig> toolConfigs = mcpToolConfigService.listByMcpConfigId(mcpConfig.getId());
                
                // 筛选出启用且 enabledList 包含当前会话来源的工具名称
                List<String> enabledToolNames = toolConfigs.stream()
                    .filter(AgentMcpToolConfig::getEnabled)
                    .filter(config -> isSourceEnabled(config.getEnabledList(), sessionSource))
                    .map(AgentMcpToolConfig::getToolName)
                    .toList();
                
                if (enabledToolNames.isEmpty()) {
                    log.debug("No enabled tools for MCP: {} with source: {}", mcpConfig.getMcpName(), sessionSource);
                    continue;
                }
                
                toolkit.registration()
                    .mcpClient(client)
                    .enableTools(enabledToolNames)
                    .apply();
                
                log.info("MCP tools loaded: {} with {} tools for source: {}", 
                    mcpConfig.getMcpName(), enabledToolNames.size(), sessionSource);
            } catch (Exception e) {
                log.error("Failed to register MCP tools: {}", mcpConfig.getMcpName(), e);
            }
        }
    }

    /**
     * 构建 SkillBox
     * 
     * @param skillBox 技能箱
     * @param toolkit 工具箱
     * @param sessionSource 会话来源
     */
    private void buildSkillBox(SkillBox skillBox, Toolkit toolkit, SessionSource sessionSource) {
        List<AgentSkillConfig> enabledSkills = skillConfigService.listEnabled();
        
        for (AgentSkillConfig config : enabledSkills) {
            // 检查 enabledList 是否包含当前会话来源
            if (!isSourceEnabled(config.getEnabledList(), sessionSource)) {
                log.debug("Skill {} not enabled for source: {}", config.getSkillPath(), sessionSource);
                continue;
            }
            
            Optional<AgentSkill> skillOpt = skillManager.loadSkill(config);
            skillOpt.ifPresent(skill -> {
                skillBox.registerSkill(skill);
                log.debug("Skill loaded: {} for source: {}", config.getSkillPath(), sessionSource);
            });
        }
    }

    /**
     * 检查 enabledList 是否包含指定的会话来源
     * 
     * @param enabledListJson enabledList 的 JSON 字符串
     * @param sessionSource 会话来源
     * @return 是否包含
     */
    private boolean isSourceEnabled(String enabledListJson, SessionSource sessionSource) {
        if (enabledListJson == null || enabledListJson.isEmpty()) {
            // 如果没有设置 enabledList，默认不启用
            return false;
        }
        
        try {
            List<String> enabledSources = objectMapper.readValue(enabledListJson, new TypeReference<List<String>>() {});
            return enabledSources.contains(sessionSource.getSourceKey());
        } catch (Exception e) {
            log.warn("Failed to parse enabledList: {}", enabledListJson, e);
            return false;
        }
    }
}
