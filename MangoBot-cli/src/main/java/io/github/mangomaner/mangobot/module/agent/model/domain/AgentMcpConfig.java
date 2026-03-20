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
 * @TableName agent_mcp_config
 */
@TableName(value ="agent_mcp_config")
@Data
public class AgentMcpConfig implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "mcp_name")
    private String mcpName;

    /**
     * 
     */
    @TableField(value = "transport_type")
    private String transportType;

    /**
     * 
     */
    @TableField(value = "connection_config")
    private String connectionConfig;

    /**
     * 
     */
    @TableField(value = "connection_status")
    private Integer connectionStatus;

    /**
     * 
     */
    @TableField(value = "enabled")
    private Boolean enabled;

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