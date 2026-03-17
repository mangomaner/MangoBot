package io.github.mangomaner.mangobot.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModelRole {
    MAIN("main", "主模型"),
    ASSISTANT("assistant", "助手模型"),
    IMAGE("image", "图片模型"),
    EMBEDDING("embedding", "向量模型");

    private final String roleKey;
    private final String description;

    public static ModelRole fromKey(String roleKey) {
        for (ModelRole role : values()) {
            if (role.getRoleKey().equals(roleKey)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown model role: " + roleKey);
    }
}
