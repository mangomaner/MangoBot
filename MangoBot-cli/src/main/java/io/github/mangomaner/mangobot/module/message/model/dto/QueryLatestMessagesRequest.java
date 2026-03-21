package io.github.mangomaner.mangobot.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "查询最新消息请求")
public class QueryLatestMessagesRequest {

    @Schema(description = "Bot ID")
    @NotBlank(message = "Bot ID不能为空")
    private String botId;

    @Schema(description = "群组ID或好友ID")
    @NotBlank(message = "群组ID或好友ID不能为空")
    private String targetId;

    @Schema(description = "查询消息数量")
    private Integer num;
}
