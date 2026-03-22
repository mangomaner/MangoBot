package io.github.mangomaner.mangobot.module.configuration.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置实体
 * 存储系统级全局配置（系统名称、日志级别等）
 */
@TableName(value = "system_configs")
@Data
public class SystemConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;

    private String configValue;

    private String configType;

    private String metadata;

    private String description;

    private String explain;

    private String category;

    private Integer editable;

    private Long createdAt;

    private Long updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
