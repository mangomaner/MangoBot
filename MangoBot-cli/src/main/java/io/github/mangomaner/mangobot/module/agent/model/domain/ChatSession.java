package io.github.mangomaner.mangobot.module.agent.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName chat_session
 */
@TableName(value ="chat_session")
@Data
public class ChatSession implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "title")
    private String title;

    /**
     * 
     */
    @TableField(value = "memory_state")
    private String memoryState;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * Bot ID（使用 String 兼容多平台）
     */
    @TableField(value = "bot_id")
    private String botId;

    /**
     * 聊天 ID（群聊ID/私聊ID，使用 String 兼容多平台）
     */
    @TableField(value = "chat_id")
    private String chatId;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 
     */
    @TableField(value = "source")
    private SessionSource source;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}