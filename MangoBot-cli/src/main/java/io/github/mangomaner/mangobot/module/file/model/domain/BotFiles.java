package io.github.mangomaner.mangobot.module.file.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName files
 */
@TableName(value ="bot_files")
@Data
public class BotFiles implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String fileType;

    /**
     * 
     */
    private String fileId;

    /**
     * 
     */
    private String url;

    /**
     * 
     */
    private String filePath;

    /**
     * 
     */
    private Integer subType;

    /**
     * 
     */
    private Integer fileSize;

    /**
     *
     */
    private String description;

    /**
     * 
     */
    private Long createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}