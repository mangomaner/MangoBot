package io.github.mangomaner.mangobot.adapter.onebot.handler.inbound.json_to_event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.OneBotBaseEvent;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.OneBotEvent;

public class OneBotEventParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static OneBotEvent parse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, OneBotBaseEvent.class);
    }
}
