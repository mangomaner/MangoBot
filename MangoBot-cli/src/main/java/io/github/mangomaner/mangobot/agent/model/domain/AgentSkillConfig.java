package io.github.mangomaner.mangobot.agent.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName agent_skill_config
 */
@TableName(value ="agent_skill_config")
@Data
public class AgentSkillConfig implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "skill_name")
    private String skillName;

    /**
     * 
     */
    @TableField(value = "description")
    private String description;

    /**
     * 
     */
    @TableField(value = "skill_path")
    private String skillPath;

    /**
     * 
     */
    @TableField(value = "bound_tool_ids")
    private String boundToolIds;

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