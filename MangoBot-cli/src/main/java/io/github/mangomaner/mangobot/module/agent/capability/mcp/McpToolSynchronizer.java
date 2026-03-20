package io.github.mangomaner.mangobot.module.agent.capability.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.service.AgentMcpConfigService;
import io.github.mangomaner.mangobot.module.agent.service.AgentMcpToolConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class McpToolSynchronizer {

    private static final int STATUS_DISCONNECTED = 0;
    private static final int STATUS_CONNECTED = 1;
    private static final int STATUS_ERROR = 2;

    private static final Duration DEFAULT_INITIALIZATION_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);

    private final AgentMcpConfigService mcpConfigService;
    private final AgentMcpToolConfigService mcpToolConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public McpClientWrapper connectAndSync(AgentMcpConfig config) {
        McpClientBuilder builder = McpClientBuilder.create(String.valueOf(config.getId()));
        Duration initTimeout = DEFAULT_INITIALIZATION_TIMEOUT;
        Duration reqTimeout = DEFAULT_REQUEST_TIMEOUT;
        
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
            
            if (connConfig.containsKey("initializationTimeout")) {
                initTimeout = Duration.ofSeconds(((Number) connConfig.get("initializationTimeout")).longValue());
            }
            if (connConfig.containsKey("timeout")) {
                reqTimeout = Duration.ofSeconds(((Number) connConfig.get("timeout")).longValue());
            }
            
            builder.initializationTimeout(initTimeout);
            builder.timeout(reqTimeout);
            
            log.info("MCP client '{}' connecting with initTimeout={}s, requestTimeout={}s", 
                config.getId(), initTimeout.getSeconds(), reqTimeout.getSeconds());
            
        } catch (Exception e) {
            log.error("Failed to parse MCP connection config: {}", config.getMcpName(), e);
            mcpConfigService.updateConnectionStatus(config.getId(), STATUS_ERROR);
            throw new RuntimeException("Failed to parse MCP connection config", e);
        }
        
        try {
            log.info("Building MCP client for config: {}", config.getId());
            McpClientWrapper client = builder.buildSync();
            
            log.info("Initializing MCP client for config: {}", config.getId());
            client.initialize().block(initTimeout);
            
            if (!client.isInitialized()) {
                throw new IllegalStateException("MCP client failed to initialize for config: " + config.getId());
            }
            
            log.info("MCP client initialized successfully for config: {}", config.getId());
            mcpConfigService.updateConnectionStatus(config.getId(), STATUS_CONNECTED);
            syncToolsFromMcp(config.getId(), client);
            return client;
        } catch (Exception e) {
            mcpConfigService.updateConnectionStatus(config.getId(), STATUS_ERROR);
            log.error("Failed to connect MCP: {}", config.getMcpName(), e);
            throw new RuntimeException("Failed to connect MCP: " + config.getMcpName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyHeaders(McpClientBuilder builder, Map<String, Object> connConfig) {
        Object headersObj = connConfig.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headers = (Map<String, String>) headersObj;
            headers.forEach(builder::header);
        }
    }

    private void syncToolsFromMcp(Integer mcpConfigId, McpClientWrapper client) {
        try {
            log.info("Syncing tools from MCP server for config: {}", mcpConfigId);
            var toolsResult = client.listTools().block(Duration.ofSeconds(30));
            if (toolsResult == null) {
                log.warn("MCP server returned null tools list for config: {}", mcpConfigId);
                return;
            }
            
            List<?> tools = toolsResult;
            if (tools == null || tools.isEmpty()) {
                log.info("No tools found for MCP config: {}", mcpConfigId);
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
                        // 默认支持所有来源
                        String allSourcesJson = buildAllSourcesJson();
                        toolConfig.setEnabledList(allSourcesJson);
                        toolConfig.setAvailableList(allSourcesJson);
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
            
            log.info("Synced {} tools for MCP config: {}", serverTools.size(), mcpConfigId);
            
        } catch (Exception e) {
            log.error("Failed to sync MCP tools for config: {}", mcpConfigId, e);
        }
    }

    public void disconnectAndCleanup(Integer mcpConfigId) {
        mcpToolConfigService.deleteByMcpConfigId(mcpConfigId);
        mcpConfigService.updateConnectionStatus(mcpConfigId, STATUS_DISCONNECTED);
        log.info("MCP disconnected and tools removed: {}", mcpConfigId);
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
