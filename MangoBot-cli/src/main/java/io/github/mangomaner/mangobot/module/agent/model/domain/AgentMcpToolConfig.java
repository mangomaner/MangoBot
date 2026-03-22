package io.github.mangomaner.mangobot.module.agent.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName agent_mcp_tool_config
 */
@TableName(value ="agent_mcp_tool_config")
@Data
public class AgentMcpToolConfig implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "mcp_config_id")
    private Integer mcpConfigId;

    /**
     * 
     */
    @TableField(value = "tool_name")
    private String toolName;

    /**
     * 
     */
    @TableField(value = "description")
    private String description;

    /**
     * 
     */
    @TableField(value = "input_schema")
    private String inputSchema;

    /**
     * 
     */
    @TableField(value = "enabled")
    private Boolean enabled;

    /**
     * 启用列表（当前启用的来源）
     */
    @TableField(value = "enabled_list")
    private String enabledList;

    /**
     * 可启用列表（可以被启用的来源）
     */
    @TableField(value = "available_list")
    private String availableList;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}