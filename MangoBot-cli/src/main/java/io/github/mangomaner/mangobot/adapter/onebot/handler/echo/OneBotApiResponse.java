package io.github.mangomaner.mangobot.adapter.onebot.handler.echo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OneBotApiResponse {
    private String status;
    private int retcode;
    private Object data;
    private String message;
    private String wording;
    private String echo;
}
