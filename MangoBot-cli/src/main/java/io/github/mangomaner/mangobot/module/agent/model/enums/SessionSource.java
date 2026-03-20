package io.github.mangomaner.mangobot.module.agent.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionSource {
    WEB("web", "Web端"),
    GROUP("group", "群聊"),
    PRIVATE("private", "私聊");

    @EnumValue
    private final String sourceKey;
    private final String description;

    public static SessionSource fromKey(String sourceKey) {
        for (SessionSource source : values()) {
            if (source.getSourceKey().equals(sourceKey)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown session source: " + sourceKey);
    }
}
