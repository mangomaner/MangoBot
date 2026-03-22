package io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message;

import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;
import lombok.Data;

import java.util.List;

@Data
public class OneBotSendingMessage {
    List<OneBotMessageSegment> message;
}
