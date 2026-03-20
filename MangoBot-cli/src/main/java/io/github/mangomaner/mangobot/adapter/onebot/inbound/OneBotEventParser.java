package io.github.mangomaner.mangobot.adapter.onebot.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.adapter.onebot.event.OneBotBaseEvent;
import io.github.mangomaner.mangobot.adapter.onebot.event.OneBotEvent;

public class OneBotEventParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static OneBotEvent parse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, OneBotBaseEvent.class);
    }
}
