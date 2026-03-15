package io.github.mangomaner.mangobot.agent.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="agent_java_tool_config")
@Data
public class AgentJavaToolConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "class_name")
    private String className;

    @TableField(value = "constructor_args")
    private String constructorArgs;

    @TableField(value = "tool_name")
    private String toolName;

    @TableField(value = "description")
    private String description;

    @TableField(value = "category")
    private String category;

    @TableField(value = "plugin_id")
    private Integer pluginId;

    @TableField(value = "enabled")
    private Boolean enabled;

    @TableField(value = "load_type")
    private String loadType;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
