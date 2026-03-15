package io.github.mangomaner.mangobot.agent.capability.mcp;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.agent.service.AgentMcpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 连接管理器
 * 
 * <p>负责管理 MCP (Model Context Protocol) 连接的生命周期：
 * <ul>
 *   <li>{@link #init()} - 启动时连接所有已启用的 MCP</li>
 *   <li>{@link #getClient(Integer)} - 获取指定 MCP 客户端</li>
 *   <li>{@link #getAllClients()} - 获取所有 MCP 客户端</li>
 *   <li>{@link #shutdown()} - 关闭所有 MCP 连接</li>
 * </ul>
 * 
 * <p>MCP 连接支持三种传输类型：
 * <ul>
 *   <li>STDIO - 通过标准输入输出通信</li>
 *   <li>SSE - 通过 Server-Sent Events 通信</li>
 *   <li>HTTP - 通过 HTTP 流式通信</li>
 * </ul>
 * 
 * @see McpToolSynchronizer
 * @see AgentMcpConfig
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpConnectionManager {

    private final AgentMcpConfigService mcpConfigService;
    private final McpToolSynchronizer mcpSynchronizer;

    /** MCP 客户端缓存：key = MCP 配置 ID */
    private final Map<Integer, McpClientWrapper> mcpClients = new ConcurrentHashMap<>();

    /**
     * 初始化 MCP 连接
     * 
     * <p>在应用启动时调用，连接所有已启用的 MCP 服务器
     */
    public void init() {
        List<AgentMcpConfig> enabledMcps = mcpConfigService.listEnabled();
        
        for (AgentMcpConfig config : enabledMcps) {
            try {
                McpClientWrapper client = mcpSynchronizer.connectAndSync(config).block();
                if (client != null) {
                    mcpClients.put(config.getId(), client);
                    log.info("MCP connected: {}", config.getMcpName());
                }
            } catch (Exception e) {
                log.error("Failed to connect MCP on startup: {}", config.getMcpName(), e);
            }
        }
    }

    /**
     * 获取 MCP 客户端
     * 
     * @param mcpConfigId MCP 配置 ID
     * @return MCP 客户端（未连接返回 null）
     */
    public McpClientWrapper getClient(Integer mcpConfigId) {
        return mcpClients.get(mcpConfigId);
    }

    /**
     * 获取所有 MCP 客户端
     * 
     * @return MCP 客户端映射（配置 ID → 客户端）
     */
    public Map<Integer, McpClientWrapper> getAllClients() {
        return new ConcurrentHashMap<>(mcpClients);
    }

    /**
     * 关闭所有 MCP 连接
     * 
     * <p>在应用关闭时调用，清理所有连接和工具配置
     */
    public void shutdown() {
        for (Map.Entry<Integer, McpClientWrapper> entry : mcpClients.entrySet()) {
            try {
                mcpSynchronizer.disconnectAndCleanup(entry.getKey());
            } catch (Exception e) {
                log.error("Failed to disconnect MCP: {}", entry.getKey(), e);
            }
        }
        mcpClients.clear();
        log.info("All MCP connections closed");
    }
}
