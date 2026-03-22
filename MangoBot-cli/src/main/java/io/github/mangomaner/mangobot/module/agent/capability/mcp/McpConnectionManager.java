package io.github.mangomaner.mangobot.module.agent.capability.mcp;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.module.agent.service.AgentMcpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class McpConnectionManager {

    private final AgentMcpConfigService mcpConfigService;
    private final McpToolSynchronizer mcpSynchronizer;

    private final Map<Integer, McpClientWrapper> mcpClients = new ConcurrentHashMap<>();

    public void init() {
        List<AgentMcpConfig> enabledMcps = mcpConfigService.listEnabled();
        
        log.info("Initializing {} MCP connections...", enabledMcps.size());
        
        for (AgentMcpConfig config : enabledMcps) {
            try {
                log.info("Connecting to MCP: {} (ID: {})", config.getMcpName(), config.getId());
                McpClientWrapper client = mcpSynchronizer.connectAndSync(config);
                mcpClients.put(config.getId(), client);
                registerClient(config.getId(), client);
                log.info("MCP connected successfully: {}", config.getMcpName());
            } catch (Exception e) {
                log.error("Failed to connect MCP on startup: {}", config.getMcpName(), e);
            }
        }
        
        log.info("MCP initialization completed. {} connections established.", mcpClients.size());
    }

    public McpClientWrapper getClient(Integer mcpConfigId) {
        return mcpClients.get(mcpConfigId);
    }

    public void registerClient(Integer mcpConfigId, McpClientWrapper client) {
        mcpClients.put(mcpConfigId, client);
        log.info("MCP client registered: ID={}", mcpConfigId);
    }

    public void unregisterClient(Integer mcpConfigId) {
        mcpClients.remove(mcpConfigId);
        log.info("MCP client unregistered: ID={}", mcpConfigId);
    }

    public Map<Integer, McpClientWrapper> getAllClients() {
        return new ConcurrentHashMap<>(mcpClients);
    }

    public void shutdown() {
        for (Map.Entry<Integer, McpClientWrapper> entry : mcpClients.entrySet()) {
            try {
                entry.getValue().close();
                mcpSynchronizer.disconnectAndCleanup(entry.getKey());
            } catch (Exception e) {
                log.error("Failed to disconnect MCP: {}", entry.getKey(), e);
            }
        }
        mcpClients.clear();
        log.info("All MCP connections closed");
    }
}
