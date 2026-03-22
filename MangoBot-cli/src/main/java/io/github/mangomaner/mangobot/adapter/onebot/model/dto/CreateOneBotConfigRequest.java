package io.github.mangomaner.mangobot.adapter.onebot.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建 OneBot 配置请求")
public class CreateOneBotConfigRequest {

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "WebSocket 服务器监听地址", defaultValue = "0.0.0.0")
    private String host = "0.0.0.0";

    @Schema(description = "WebSocket 服务器监听端口", defaultValue = "8080")
    private Integer port = 8080;

    @Schema(description = "WebSocket 路径", defaultValue = "/")
    private String path = "/";

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "描述")
    private String description;
}
