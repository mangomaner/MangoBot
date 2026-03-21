package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 窗口抖动（戳一戳）消息段
 * [CQ:shake]
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ShakeSegment extends OneBotMessageSegment {
    private ShakeData data;

    @Data
    public static class ShakeData {
    }
}
