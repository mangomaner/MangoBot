package io.github.mangomaner.mangobot.module.configuration.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型测试结果视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模型测试结果")
public class ModelTestResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "响应内容")
    private String content;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "耗时（毫秒）")
    private Long duration;
}
