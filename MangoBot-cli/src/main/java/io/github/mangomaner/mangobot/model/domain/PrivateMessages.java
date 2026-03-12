package io.github.mangomaner.mangobot.model.domain;

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
     * 
     */
    private Long botId;

    /**
     * 
     */
    private Long friendId;

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