package io.github.mangomaner.mangobot.adapter.onebot.outbound;

import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;
import lombok.Data;

import java.util.List;

@Data
public class OneBotSendingMessage {
    List<OneBotMessageSegment> message;
}
