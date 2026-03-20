package io.github.mangomaner.mangobot.module.message.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName group_messages
 */
@TableName(value ="group_messages")
@Data
public class GroupMessages implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long botId;

    /**
     * 
     */
    private Long groupId;

    /**
     * 
     */
    private Integer messageId;

    /**
     * 
     */
    private Long senderId;

    /**
     * 
     */
    private String messageSegments;

    /**
     * 
     */
    private Long messageTime;

    @TableLogic
    private Integer isDelete;

    /**
     * 
     */
    private String parseMessage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}