package io.github.mangomaner.mangobot.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 对话消息视图对象
 */
@Data
@Schema(description = "对话消息信息")
public class ChatMessageWebVO {

    @Schema(description = "消息ID")
    private Integer id;

    @Schema(description = "关联的会话ID")
    private Integer sessionId;

    @Schema(description = "角色：user, assistant, system")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "创建时间")
    private Date createTime;
}
