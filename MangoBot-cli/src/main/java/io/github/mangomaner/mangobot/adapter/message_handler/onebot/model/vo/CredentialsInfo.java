package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CredentialsInfo {
    private String cookies;
    @JsonProperty("csrf_token")
    private int csrfToken;
    private int token; // For get_csrf_token only
}
