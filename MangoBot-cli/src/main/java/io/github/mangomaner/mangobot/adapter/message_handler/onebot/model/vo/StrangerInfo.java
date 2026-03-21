package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StrangerInfo {
    @JsonProperty("user_id")
    private long userId;
    
    private String nickname;
    
    private String sex;
    
    private int age;
}
