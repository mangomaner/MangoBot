package io.github.mangomaner.mangobot.module.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 用量统计视图对象
 * <p>
 * 用于记录和展示 LLM 调用的 Token 消耗情况
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token用量统计信息")
public class TokenUsageVO {

    @Schema(description = "输入Token数量")
    private Long inputTokens;

    @Schema(description = "输出Token数量")
    private Long outputTokens;

    @Schema(description = "总Token数量")
    private Long totalTokens;

    @Schema(description = "响应时间(秒)")
    private Double time;

    @Schema(description = "缓存Token数量")
    private Long cachedTokens;
}
