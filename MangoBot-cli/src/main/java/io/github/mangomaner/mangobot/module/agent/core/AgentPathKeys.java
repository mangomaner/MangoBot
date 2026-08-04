package io.github.mangomaner.mangobot.module.agent.core;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;

/**
 * 会话路径键工具：userId / sessionId 会作为文件系统路径段使用（本机模式
 * &lt;workspace&gt;/&lt;userId&gt;/...、sessions/&lt;sessionId&gt;.log.jsonl），
 * 不能含 ':'、'/'、'\' 等字符（Windows 下冒号直接抛 InvalidPathException）。
 * 统一用 '_' 连接；前端需要展示原始格式时在展示层还原。
 */
public final class AgentPathKeys {

    private AgentPathKeys() {
    }

    public static String userId(String botId, SessionSource source, String chatId) {
        String src = source != null ? source.getSourceKey() : "web";
        return safe("bot_" + botId + "_" + src + "_" + effective(chatId, null));
    }

    public static String sessionId(String botId, SessionSource source, String chatId) {
        String src = source != null ? source.getSourceKey() : "web";
        return safe(src + "_" + effective(chatId, null));
    }

    /** chatId 为空时用备用键（如 DB 会话 ID） */
    public static String userId(String botId, SessionSource source, String chatId, String fallbackKey) {
        String src = source != null ? source.getSourceKey() : "web";
        return safe("bot_" + botId + "_" + src + "_" + effective(chatId, fallbackKey));
    }

    public static String sessionId(SessionSource source, String chatId, String fallbackKey) {
        String src = source != null ? source.getSourceKey() : "web";
        return safe(src + "_" + effective(chatId, fallbackKey));
    }

    private static String effective(String chatId, String fallbackKey) {
        return (chatId != null && !chatId.isBlank()) ? chatId : (fallbackKey != null ? fallbackKey : "unknown");
    }

    private static String safe(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
