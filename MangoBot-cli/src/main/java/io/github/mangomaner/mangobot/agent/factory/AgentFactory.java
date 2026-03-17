package io.github.mangomaner.mangobot.agent.factory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.agent.capability.mcp.McpConnectionManager;
import io.github.mangomaner.mangobot.agent.capability.skill.SkillManager;
import io.github.mangomaner.mangobot.agent.capability.tool.JavaToolLoader;
import io.github.mangomaner.mangobot.agent.hook.StreamingToolHook;
import io.github.mangomaner.mangobot.agent.manager.MemoryManager;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.agent.service.AgentJavaToolConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentMcpConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentMcpToolConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentSkillConfigService;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.github.mangomaner.mangobot.api.ModelRole;
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
    
    private final JavaToolLoader javaToolLoader;
    private final McpConnectionManager mcpConnectionManager;
    private final SkillManager skillManager;

    /**
     * 创建 Agent
     * 
     * @param sessionId 会话 ID
     * @return ReActAgent 实例
     */
    public ReActAgent createAgent(Integer sessionId) {
        String agentName = "MangoBot-" + sessionId;
        log.info("Creating ReActAgent for session: {}, name: {}", sessionId, agentName);

        AutoContextMemory memory = memoryManager.getOrCreateMemory(sessionId);
        Toolkit toolkit = new Toolkit();
        SkillBox skillBox = new SkillBox(toolkit);

        loadJavaTools(toolkit);
        loadMcpTools(toolkit);
        buildSkillBox(skillBox, toolkit);

        return ReActAgent.builder()
                .name(agentName)
                .sysPrompt("")
                .model(MangoModelApi.getModel(ModelRole.MAIN))
                .memory(memory)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .enableMetaTool(true)
                .hooks(List.of(streamingToolHook))
                .build();
    }

    /**
     * 加载 Java 工具
     */
    private void loadJavaTools(Toolkit toolkit) {
        List<AgentJavaToolConfig> enabledTools = javaToolConfigService.listEnabled();
        
        for (AgentJavaToolConfig config : enabledTools) {
            javaToolLoader.loadTool(config).ifPresent(tool -> {
                toolkit.registerTool(tool);
                log.debug("Java tool loaded: {}", config.getClassName());
            });
        }
    }

    /**
     * 加载 MCP 工具
     */
    private void loadMcpTools(Toolkit toolkit) {
        List<AgentMcpConfig> enabledMcps = mcpConfigService.listEnabled();
        Map<Integer, McpClientWrapper> mcpClients = mcpConnectionManager.getAllClients();
        
        for (AgentMcpConfig mcpConfig : enabledMcps) {
            McpClientWrapper client = mcpClients.get(mcpConfig.getId());
            
            if (client != null) {
                try {
                    List<String> enabledToolNames = mcpToolConfigService
                        .listEnabledToolNamesByMcpConfigId(mcpConfig.getId());
                    
                    toolkit.registration()
                        .mcpClient(client)
                        .enableTools(enabledToolNames)
                        .apply();
                    
                    log.debug("MCP tools loaded: {} with {} tools", 
                        mcpConfig.getMcpName(), enabledToolNames.size());
                } catch (Exception e) {
                    log.error("Failed to register MCP tools: {}", mcpConfig.getMcpName(), e);
                }
            }
        }
    }

    /**
     * 构建 SkillBox
     */
    private void buildSkillBox(SkillBox skillBox, Toolkit toolkit) {
        List<AgentSkillConfig> enabledSkills = skillConfigService.listEnabled();
        
        for (AgentSkillConfig config : enabledSkills) {
            Optional<AgentSkill> skillOpt = skillManager.loadSkill(config);
            skillOpt.ifPresent(skill -> {
                skillBox.registerSkill(skill);
                log.debug("Skill loaded: {}", config.getSkillPath());
            });
        }
    }
}
