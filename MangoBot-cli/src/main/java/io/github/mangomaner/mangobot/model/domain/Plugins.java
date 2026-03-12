package io.github.mangomaner.mangobot.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName plugins
 */
@TableName(value ="plugins")
@Data
public class Plugins implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String pluginName;

    /**
     *
     */
    private String jarName;

    /**
     * 
     */
    private String author;

    /**
     * 
     */
    private String version;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private Integer enabled;

    /**
     * 
     */
    private Integer enabledWeb;

    /**
     *
     */
    private String packageName;

    /**
     * 
     */
    private Long createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}