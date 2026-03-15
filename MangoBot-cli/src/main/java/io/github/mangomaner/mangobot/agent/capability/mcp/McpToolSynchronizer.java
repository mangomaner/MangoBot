package io.github.mangomaner.mangobot.agent.capability.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.agent.service.AgentMcpConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentMcpToolConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP 工具同步器
 * 
 * <p>负责 MCP 连接的建立、工具同步和断开清理：
 * <ul>
 *   <li>{@link #connectAndSync(AgentMcpConfig)} - 连接 MCP 并同步工具列表</li>
 *   <li>{@link #disconnectAndCleanup(Integer)} - 断开连接并清理工具配置</li>
 * </ul>
 * 
 * <p>工具同步逻辑：
 * <ol>
 *   <li>连接成功后调用 MCP 服务器的 listTools</li>
 *   <li>将新工具写入数据库（默认启用）</li>
 *   <li>删除服务器不再提供的工具</li>
 * </ol>
 * 
 * @see McpConnectionManager
 * @see AgentMcpToolConfig
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class McpToolSynchronizer {

    /** 连接状态：断开 */
    private static final int STATUS_DISCONNECTED = 0;
    
    /** 连接状态：已连接 */
    private static final int STATUS_CONNECTED = 1;
    
    /** 连接状态：错误 */
    private static final int STATUS_ERROR = 2;

    private final AgentMcpConfigService mcpConfigService;
    private final AgentMcpToolConfigService mcpToolConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接 MCP 并同步工具
     * 
     * <p>根据配置的传输类型建立连接：
     * <ul>
     *   <li>STDIO - 解析 command 和 args</li>
     *   <li>SSE - 解析 url 和 headers</li>
     *   <li>HTTP - 解析 url 和 headers</li>
     * </ul>
     * 
     * @param config MCP 配置
     * @return MCP 客户端（异步）
     */
    public Mono<McpClientWrapper> connectAndSync(AgentMcpConfig config) {
        McpClientBuilder builder = McpClientBuilder.create(String.valueOf(config.getId()));
        
        try {
            Map<String, Object> connConfig = objectMapper.readValue(config.getConnectionConfig(), Map.class);
            
            switch (config.getTransportType()) {
                case "STDIO" -> {
                    String command = (String) connConfig.get("command");
                    List<String> args = (List<String>) connConfig.get("args");
                    if (args != null) {
                        builder.stdioTransport(command, args.toArray(new String[0]));
                    } else {
                        builder.stdioTransport(command);
                    }
                }
                case "SSE" -> {
                    builder.sseTransport((String) connConfig.get("url"));
                    applyHeaders(builder, connConfig);
                }
                case "HTTP" -> {
                    builder.streamableHttpTransport((String) connConfig.get("url"));
                    applyHeaders(builder, connConfig);
                }
            }
            
            if (connConfig.containsKey("timeout")) {
                builder.timeout(Duration.ofSeconds(((Number) connConfig.get("timeout")).longValue()));
            }
            
        } catch (Exception e) {
            log.error("Failed to parse MCP connection config: {}", config.getMcpName(), e);
            return Mono.error(e);
        }
        
        return builder.buildAsync()
            .doOnSuccess(client -> {
                mcpConfigService.updateConnectionStatus(config.getId(), STATUS_CONNECTED);
                syncToolsFromMcp(config.getId(), client);
            })
            .doOnError(e -> {
                mcpConfigService.updateConnectionStatus(config.getId(), STATUS_ERROR);
                log.error("Failed to connect MCP: {}", config.getMcpName(), e);
            });
    }

    /**
     * 应用 HTTP 头
     */
    @SuppressWarnings("unchecked")
    private void applyHeaders(McpClientBuilder builder, Map<String, Object> connConfig) {
        Object headersObj = connConfig.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headers = (Map<String, String>) headersObj;
            headers.forEach(builder::header);
        }
    }

    /**
     * 从 MCP 服务器同步工具列表
     * 
     * <p>同步策略：
     * <ul>
     *   <li>新工具：写入数据库，默认启用</li>
     *   <li>已删除的工具：从数据库删除</li>
     *   <li>已存在的工具：保留配置（不覆盖启用状态）</li>
     * </ul>
     */
    private void syncToolsFromMcp(Integer mcpConfigId, McpClientWrapper client) {
        try {
            var toolsResult = client.listTools().block();
            if (toolsResult == null) {
                return;
            }
            
            List<?> tools = toolsResult;
            if (tools == null || tools.isEmpty()) {
                return;
            }
            
            Set<String> existingTools = new HashSet<>();
            mcpToolConfigService.listByMcpConfigId(mcpConfigId)
                .forEach(t -> existingTools.add(t.getToolName()));
            
            Set<String> serverTools = new HashSet<>();
            for (Object toolObj : tools) {
                try {
                    Map<String, Object> toolMap = objectMapper.convertValue(toolObj, Map.class);
                    String toolName = (String) toolMap.get("name");
                    if (toolName == null) continue;
                    
                    serverTools.add(toolName);
                    
                    if (!existingTools.contains(toolName)) {
                        AgentMcpToolConfig toolConfig = new AgentMcpToolConfig();
                        toolConfig.setMcpConfigId(mcpConfigId);
                        toolConfig.setToolName(toolName);
                        toolConfig.setDescription((String) toolMap.get("description"));
                        try {
                            Object inputSchema = toolMap.get("inputSchema");
                            if (inputSchema != null) {
                                toolConfig.setInputSchema(objectMapper.writeValueAsString(inputSchema));
                            } else {
                                toolConfig.setInputSchema("{}");
                            }
                        } catch (Exception e) {
                            toolConfig.setInputSchema("{}");
                        }
                        toolConfig.setEnabled(true);
                        mcpToolConfigService.save(toolConfig);
                        log.info("New MCP tool registered: {}::{}", mcpConfigId, toolName);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process tool object", e);
                }
            }
            
            for (String existingTool : existingTools) {
                if (!serverTools.contains(existingTool)) {
                    mcpToolConfigService.deleteByMcpConfigIdAndToolName(mcpConfigId, existingTool);
                    log.info("MCP tool removed: {}::{}", mcpConfigId, existingTool);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to sync MCP tools for config: {}", mcpConfigId, e);
        }
    }

    /**
     * 断开连接并清理工具配置
     * 
     * @param mcpConfigId MCP 配置 ID
     */
    public void disconnectAndCleanup(Integer mcpConfigId) {
        mcpToolConfigService.deleteByMcpConfigId(mcpConfigId);
        mcpConfigService.updateConnectionStatus(mcpConfigId, STATUS_DISCONNECTED);
        log.info("MCP disconnected and tools removed: {}", mcpConfigId);
    }
}
