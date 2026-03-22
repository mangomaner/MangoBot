package io.github.mangomaner.mangobot.module.message.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName private_messages
 */
@TableName(value ="private_messages")
@Data
public class PrivateMessages implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Bot ID（使用 String 兼容多平台）
     */
    private String botId;

    /**
     * 好友 ID（使用 String 兼容多平台）
     */
    private String friendId;

    /**
     * 消息 ID（使用 String 兼容多平台）
     */
    private String messageId;

    /**
     * 发送者 ID（使用 String 兼容多平台）
     */
    private String senderId;

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