package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginInfo {
    @JsonProperty("user_id")
    private long userId;
    
    private String nickname;
}
