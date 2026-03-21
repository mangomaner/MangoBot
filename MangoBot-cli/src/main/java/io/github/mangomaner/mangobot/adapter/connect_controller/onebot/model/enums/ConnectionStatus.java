package io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.enums;

import lombok.Getter;

@Getter
public enum ConnectionStatus {

    NOT_STARTED(0, "未启动"),
    RUNNING(1, "运行中"),
    STOPPED(2, "已停止"),
    ERROR(3, "错误");

    private final int code;
    private final String description;

    ConnectionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ConnectionStatus fromCode(int code) {
        for (ConnectionStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return NOT_STARTED;
    }
}
