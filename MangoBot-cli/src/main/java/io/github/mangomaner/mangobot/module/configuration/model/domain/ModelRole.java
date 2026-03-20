package io.github.mangomaner.mangobot.module.configuration.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 模型角色实体
 * 定义模型角色与模型配置的映射关系
 */
@TableName(value = "model_roles")
@Data
public class ModelRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleKey;

    private String roleName;

    private Long modelConfigId;

    private String description;

    private Long createdAt;

    private Long updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
