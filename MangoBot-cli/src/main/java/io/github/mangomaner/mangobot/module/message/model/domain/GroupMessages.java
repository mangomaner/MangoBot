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
     * Bot ID（使用 String 兼容多平台）
     */
    private String botId;

    /**
     * 群组 ID（使用 String 兼容多平台）
     */
    private String groupId;

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