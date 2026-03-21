package io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新 OneBot 配置请求")
public class UpdateOneBotConfigRequest {

    @Schema(description = "配置ID", required = true)
    private Long id;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "WebSocket 服务器监听地址")
    private String host;

    @Schema(description = "WebSocket 服务器监听端口")
    private Integer port;

    @Schema(description = "WebSocket 路径")
    private String path;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "描述")
    private String description;
}
