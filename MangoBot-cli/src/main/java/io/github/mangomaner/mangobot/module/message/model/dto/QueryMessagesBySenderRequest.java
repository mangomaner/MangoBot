package io.github.mangomaner.mangobot.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "查询发送者最新消息请求")
public class QueryMessagesBySenderRequest {

    @Schema(description = "Bot ID")
    @NotBlank(message = "Bot ID不能为空")
    private String botId;

    @Schema(description = "发送者ID")
    @NotBlank(message = "发送者ID不能为空")
    private String senderId;
}
