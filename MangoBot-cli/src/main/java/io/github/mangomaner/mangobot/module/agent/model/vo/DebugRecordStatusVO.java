package io.github.mangomaner.mangobot.module.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话调试记录状态
 */
@Data
@Schema(description = "会话调试记录状态")
public class DebugRecordStatusVO {

    @Schema(description = "记录开关是否开启")
    private Boolean enabled;

    @Schema(description = "已记录的模型调用次数")
    private Integer recordCount;
}
