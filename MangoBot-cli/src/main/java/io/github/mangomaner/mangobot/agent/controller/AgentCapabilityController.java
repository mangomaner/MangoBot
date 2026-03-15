package io.github.mangomaner.mangobot.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.mangomaner.mangobot.agent.capability.mcp.McpConnectionManager;
import io.github.mangomaner.mangobot.agent.capability.mcp.McpToolSynchronizer;
import io.github.mangomaner.mangobot.agent.capability.skill.SkillManager;
import io.github.mangomaner.mangobot.agent.capability.tool.JavaToolLoader;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.agent.model.dto.CreateMcpConfigRequest;
import io.github.mangomaner.mangobot.agent.model.dto.CreateSkillRequest;
import io.github.mangomaner.mangobot.agent.model.dto.UpdateSkillRequest;
import io.github.mangomaner.mangobot.agent.model.vo.JavaToolVO;
import io.github.mangomaner.mangobot.agent.model.vo.McpConfigVO;
import io.github.mangomaner.mangobot.agent.model.vo.McpToolVO;
import io.github.mangomaner.mangobot.agent.model.vo.SkillVO;
import io.github.mangomaner.mangobot.agent.service.AgentJavaToolConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentMcpConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentMcpToolConfigService;
import io.github.mangomaner.mangobot.agent.service.AgentSkillConfigService;
import io.github.mangomaner.mangobot.common.BaseResponse;
import io.github.mangomaner.mangobot.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 能力管理控制器
 * 
 * <p>提供以下能力的管理接口：
 * <ul>
 *   <li>Java 工具 - 注册、启用/禁用、注销</li>
 *   <li>MCP 连接 - 创建、连接、断开、删除</li>
 *   <li>Skill - 创建、编辑、启用/禁用、删除</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/capability")
@Tag(name = "Agent 能力管理")
@RequiredArgsConstructor
public class AgentCapabilityController {

    private final AgentJavaToolConfigService javaToolConfigService;
    private final AgentMcpConfigService mcpConfigService;
    private final AgentMcpToolConfigService mcpToolConfigService;
    private final AgentSkillConfigService skillConfigService;
    
    private final JavaToolLoader javaToolLoader;
    private final McpToolSynchronizer mcpSynchronizer;
    private final McpConnectionManager mcpConnectionManager;
    private final SkillManager skillManager;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Java 工具 ====================

    @GetMapping("/java-tools")
    @Operation(summary = "获取 Java 工具列表")
    public BaseResponse<List<JavaToolVO>> listJavaTools() {
        List<JavaToolVO> result = javaToolConfigService.list().stream()
            .map(config -> JavaToolVO.from(config, javaToolLoader))
            .collect(Collectors.toList());
        return new BaseResponse<>(0, result, "");
    }

    @PutMapping("/java-tools/{id}/toggle")
    @Operation(summary = "启用/禁用 Java 工具")
    public BaseResponse<Boolean> toggleJavaTool(@PathVariable Integer id) {
        AgentJavaToolConfig config = javaToolConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean newEnabled = !Boolean.TRUE.equals(config.getEnabled());
        config.setEnabled(newEnabled);
        javaToolConfigService.updateById(config);
        return new BaseResponse<>(0, newEnabled, "");
    }

    @DeleteMapping("/java-tools/{id}")
    @Operation(summary = "注销 Java 工具")
    public BaseResponse<Boolean> unregisterJavaTool(@PathVariable Integer id) {
        AgentJavaToolConfig config = javaToolConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        javaToolConfigService.removeById(id);
        return new BaseResponse<>(0, true, "");
    }

    // ==================== MCP 连接 ====================

    @GetMapping("/mcp")
    @Operation(summary = "获取 MCP 连接列表")
    public BaseResponse<List<McpConfigVO>> listMcpConnections() {
        List<McpConfigVO> result = mcpConfigService.list().stream()
            .map(McpConfigVO::from)
            .collect(Collectors.toList());
        return new BaseResponse<>(0, result, "");
    }

    @PostMapping("/mcp")
    @Operation(summary = "创建 MCP 连接")
    public BaseResponse<AgentMcpConfig> createMcpConnection(@RequestBody CreateMcpConfigRequest request) {
        AgentMcpConfig config = new AgentMcpConfig();
        config.setMcpName(request.getMcpName());
        config.setTransportType(request.getTransportType());
        config.setConnectionConfig(request.getConnectionConfig());
        config.setEnabled(false);
        config.setConnectionStatus(0);
        mcpConfigService.save(config);
        return new BaseResponse<>(0, config, "");
    }

    @PutMapping("/mcp/{id}/toggle")
    @Operation(summary = "启用/禁用 MCP 连接")
    public BaseResponse<Boolean> toggleMcp(@PathVariable Integer id) {
        AgentMcpConfig config = mcpConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        
        boolean newEnabled = !Boolean.TRUE.equals(config.getEnabled());
        
        if (newEnabled) {
            try {
                McpClientWrapper client = mcpSynchronizer.connectAndSync(config).block();
                if (client != null) {
                    config.setEnabled(true);
                    mcpConfigService.updateById(config);
                }
            } catch (Exception e) {
                log.error("Failed to connect MCP: {}", config.getMcpName(), e);
                return new BaseResponse<>(ErrorCode.OPERATION_ERROR.getCode(), null, "连接失败: " + e.getMessage());
            }
        } else {
            mcpSynchronizer.disconnectAndCleanup(id);
            config.setEnabled(false);
            mcpConfigService.updateById(config);
        }
        
        return new BaseResponse<>(0, newEnabled, "");
    }

    @DeleteMapping("/mcp/{id}")
    @Operation(summary = "删除 MCP 连接")
    public BaseResponse<Boolean> deleteMcp(@PathVariable Integer id) {
        mcpSynchronizer.disconnectAndCleanup(id);
        mcpConfigService.removeById(id);
        return new BaseResponse<>(0, true, "");
    }

    @GetMapping("/mcp/{mcpConfigId}/tools")
    @Operation(summary = "获取 MCP 下的工具列表")
    public BaseResponse<List<McpToolVO>> listMcpTools(@PathVariable Integer mcpConfigId) {
        List<McpToolVO> result = mcpToolConfigService.listByMcpConfigId(mcpConfigId).stream()
            .map(McpToolVO::from)
            .collect(Collectors.toList());
        return new BaseResponse<>(0, result, "");
    }

    @PutMapping("/mcp-tools/{id}/toggle")
    @Operation(summary = "启用/禁用 MCP 工具")
    public BaseResponse<Boolean> toggleMcpTool(@PathVariable Integer id) {
        AgentMcpToolConfig config = mcpToolConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean newEnabled = !Boolean.TRUE.equals(config.getEnabled());
        config.setEnabled(newEnabled);
        mcpToolConfigService.updateById(config);
        return new BaseResponse<>(0, newEnabled, "");
    }

    // ==================== Skill ====================

    @GetMapping("/skills")
    @Operation(summary = "获取 Skill 列表")
    public BaseResponse<List<SkillVO>> listSkills() {
        List<SkillVO> result = skillConfigService.list().stream()
            .map(config -> SkillVO.from(config, skillManager))
            .collect(Collectors.toList());
        return new BaseResponse<>(0, result, "");
    }

    @PostMapping("/skills")
    @Operation(summary = "创建 Skill")
    public BaseResponse<AgentSkillConfig> createSkill(@RequestBody CreateSkillRequest request) throws IOException {
        skillManager.createSkillDirectory(request.getSkillPath());
        
        String content = buildSkillContent(request);
        skillManager.writeSkillContent(request.getSkillPath(), content);
        
        AgentSkillConfig config = new AgentSkillConfig();
        config.setSkillName(request.getSkillName());
        config.setDescription(request.getDescription());
        config.setSkillPath(request.getSkillPath());
        if (request.getBoundToolIds() != null && !request.getBoundToolIds().isEmpty()) {
            config.setBoundToolIds(objectMapper.writeValueAsString(request.getBoundToolIds()));
        }
        config.setEnabled(false);
        skillConfigService.save(config);
        
        return new BaseResponse<>(0, config, "");
    }

    @PutMapping("/skills/{id}")
    @Operation(summary = "更新 Skill")
    public BaseResponse<Boolean> updateSkill(@PathVariable Integer id, @RequestBody UpdateSkillRequest request) throws IOException {
        AgentSkillConfig config = skillConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        
        config.setSkillName(request.getSkillName());
        config.setDescription(request.getDescription());
        if (request.getBoundToolIds() != null) {
            config.setBoundToolIds(objectMapper.writeValueAsString(request.getBoundToolIds()));
        }
        skillConfigService.updateById(config);
        
        if (request.getSkillContent() != null) {
            skillManager.writeSkillContent(config.getSkillPath(), request.getSkillContent());
        }
        
        return new BaseResponse<>(0, true, "");
    }

    @PutMapping("/skills/{id}/toggle")
    @Operation(summary = "启用/禁用 Skill")
    public BaseResponse<Boolean> toggleSkill(@PathVariable Integer id) {
        AgentSkillConfig config = skillConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean newEnabled = !Boolean.TRUE.equals(config.getEnabled());
        config.setEnabled(newEnabled);
        skillConfigService.updateById(config);
        return new BaseResponse<>(0, newEnabled, "");
    }

    @DeleteMapping("/skills/{id}")
    @Operation(summary = "删除 Skill")
    public BaseResponse<Boolean> deleteSkill(@PathVariable Integer id) throws IOException {
        AgentSkillConfig config = skillConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        skillManager.deleteSkillDirectory(config.getSkillPath());
        skillConfigService.removeById(id);
        return new BaseResponse<>(0, true, "");
    }

    @GetMapping("/skills/{id}/content")
    @Operation(summary = "获取 Skill 文件内容")
    public BaseResponse<String> getSkillContent(@PathVariable Integer id) throws IOException {
        AgentSkillConfig config = skillConfigService.getById(id);
        if (config == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR);
        }
        String content = skillManager.readSkillContent(config.getSkillPath());
        return new BaseResponse<>(0, content, "");
    }

    private String buildSkillContent(CreateSkillRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("name: ").append(request.getSkillName()).append("\n");
        sb.append("description: ").append(request.getDescription() != null ? request.getDescription() : "").append("\n");
        if (request.getBoundToolIds() != null && !request.getBoundToolIds().isEmpty()) {
            sb.append("bound_tool_ids: ").append(request.getBoundToolIds()).append("\n");
        }
        sb.append("---\n\n");
        if (request.getSkillContent() != null) {
            sb.append(request.getSkillContent());
        }
        return sb.toString();
    }
}
