package io.github.mangomaner.mangobot.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "搜索消息请求")
public class SearchMessagesRequest {

    @Schema(description = "Bot ID")
    @NotBlank(message = "Bot ID不能为空")
    private String botId;

    @Schema(description = "群组ID或好友ID")
    @NotBlank(message = "群组ID或好友ID不能为空")
    private String targetId;

    @Schema(description = "搜索关键词")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
}
