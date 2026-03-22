package io.github.mangomaner.mangobot.module.configuration.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Bot 配置实体
 * 存储 Bot 级别配置（白名单、黑名单等）
 */
@TableName(value = "bot_configs")
@Data
public class BotConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String botId;

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
