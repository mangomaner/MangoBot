package io.github.mangomaner.mangobot.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "根据消息ID查询前num条消息请求")
public class QueryMessagesByMessageIdRequest {

    @Schema(description = "Bot ID")
    @NotBlank(message = "Bot ID不能为空")
    private String botId;

    @Schema(description = "群组ID或好友ID")
    @NotBlank(message = "群组ID或好友ID不能为空")
    private String targetId;

    @Schema(description = "消息ID")
    @NotBlank(message = "消息ID不能为空")
    private String messageId;

    @Schema(description = "查询数量")
    private Integer num;
}
