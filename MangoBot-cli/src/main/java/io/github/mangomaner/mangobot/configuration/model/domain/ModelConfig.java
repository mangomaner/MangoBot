package io.github.mangomaner.mangobot.configuration.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 模型配置实体
 */
@TableName(value = "model_configs")
@Data
public class ModelConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String modelName;

    private Long providerId;

    private Double temperature;

    private Integer maxTokens;

    private Double topP;

    private Integer timeout;

    private String description;

    private Integer isEnabled;

    private Long createdAt;

    private Long updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
