package io.github.mangomaner.mangobot.adapter;

import lombok.Getter;

@Getter
public enum PlatformType {

    ONEBOT_QQ("onebot_qq", "OneBot QQ", "OneBot 协议 QQ 机器人连接");

    private final String code;
    private final String name;
    private final String description;

    PlatformType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static PlatformType fromCode(String code) {
        for (PlatformType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
