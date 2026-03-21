package io.github.mangomaner.mangobot.adapter.message_handler.onebot.inbound.json_to_event;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.OneBotEvent;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.message.OneBotPrivateMessageEvent;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.meta.OneBotHeartbeatEvent;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.meta.OneBotLifecycleEvent;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.notice.*;

import java.io.IOException;

public class OneBotEventDeserializer extends StdDeserializer<OneBotEvent> {

    public OneBotEventDeserializer() {
        this(null);
    }

    public OneBotEventDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public OneBotEvent deserialize(JsonParser p, DeserializationContext ctxt) throws IOException{
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        
        if (!node.has("post_type")) {
            return null;
        }
        
        String postType = node.get("post_type").asText();
        Class<? extends OneBotEvent> targetClass = null;

        switch (postType) {
            case "message":
                if (node.has("message_type")) {
                    String messageType = node.get("message_type").asText();
                    if ("group".equals(messageType)) {
                        targetClass = OneBotGroupMessageEvent.class;
                    } else if ("private".equals(messageType)) {
                        targetClass = OneBotPrivateMessageEvent.class;
                    }
                }
                break;
            case "meta_event":
                if (node.has("meta_event_type")) {
                    String metaType = node.get("meta_event_type").asText();
                    if ("heartbeat".equals(metaType)) {
                        targetClass = OneBotHeartbeatEvent.class;
                    } else if ("lifecycle".equals(metaType)) {
                        targetClass = OneBotLifecycleEvent.class;
                    }
                }
                break;
            case "notice":
                if (node.has("notice_type")) {
                    String noticeType = node.get("notice_type").asText();
                    switch (noticeType) {
                        case "notify":
                            targetClass = PokeEvent.class;
                            break;
                        case "group_decrease":
                            targetClass = OneBotGroupDecreaseEvent.class;
                            break;
                        case "group_increase":
                            targetClass = OneBotGroupIncreaseEvent.class;
                            break;
                        case "group_ban":
                            targetClass = OneBotGroupBanEvent.class;
                            break;
                        case "essence":
                            targetClass = OneBotEssenceEvent.class;
                            break;
                        case "group_recall":
                            targetClass = OneBotGroupRecallEvent.class;
                            break;
                    }
                }
                break;
        }

        if (targetClass != null) {
            return mapper.treeToValue(node, targetClass);
        }
        
        // Fallback or ignore
        return null;
    }
}
