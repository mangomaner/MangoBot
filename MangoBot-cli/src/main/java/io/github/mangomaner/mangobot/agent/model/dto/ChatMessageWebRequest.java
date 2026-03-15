package io.github.mangomaner.mangobot.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送对话消息请求DTO
 */
@Data
@Schema(description = "发送对话消息请求")
public class ChatMessageWebRequest {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "关联的会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "角色：user, assistant, system，默认为user")
    private String role = "user";

    @Schema(description = "元数据，JSON格式")
    private String metadata;
}
