package io.github.mangomaner.mangobot.module.agent.debug;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 调试「记录」：按会话持久化记录开关，并把每次模型调用的完整上下文追加到文件。
 *
 * <p>文件布局（相对应用根目录）：
 * <ul>
 *   <li>{@code data/debug/settings.json}：{@code {"<sessionId>": true, ...}}，记录开关，重启不丢；</li>
 *   <li>{@code data/debug/<sessionId>.jsonl}：每次模型调用一行 JSON
 *       {@code {"time": "...", "input": {"messages": [...], "tools": [...]}, "usage": {...}}}。</li>
 * </ul>
 */
@Slf4j
@Component
public class AgentDebugRecorder {

    private static final String DEBUG_DIR = "data/debug";
    private static final String SETTINGS_FILE = "settings.json";
    private static final TypeReference<Map<String, Boolean>> SETTINGS_TYPE = new TypeReference<>() {};

    private final Object settingsLock = new Object();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** sessionId -> 是否开启记录（仅保留开启项） */
    private final ConcurrentHashMap<Integer, Boolean> recording = new ConcurrentHashMap<>();
    private volatile boolean settingsLoaded = false;

    private Path debugDir() {
        return FileUtils.resolvePath(DEBUG_DIR);
    }

    private Path settingsPath() {
        return debugDir().resolve(SETTINGS_FILE);
    }

    private Path recordFile(Integer sessionId) {
        return debugDir().resolve(sessionId + ".jsonl");
    }

    private void ensureLoaded() {
        if (settingsLoaded) {
            return;
        }
        synchronized (settingsLock) {
            if (settingsLoaded) {
                return;
            }
            Path path = settingsPath();
            if (Files.exists(path)) {
                try {
                    Map<String, Boolean> map = objectMapper.readValue(Files.readString(path, StandardCharsets.UTF_8), SETTINGS_TYPE);
                    map.forEach((key, value) -> {
                        if (Boolean.TRUE.equals(value)) {
                            recording.put(Integer.valueOf(key), true);
                        }
                    });
                } catch (Exception e) {
                    log.warn("Failed to load debug settings", e);
                }
            }
            settingsLoaded = true;
        }
    }

    public boolean isRecording(Integer sessionId) {
        if (sessionId == null) {
            return false;
        }
        ensureLoaded();
        return Boolean.TRUE.equals(recording.get(sessionId));
    }

    public void setRecording(Integer sessionId, boolean enabled) {
        if (sessionId == null) {
            return;
        }
        ensureLoaded();
        synchronized (settingsLock) {
            if (enabled) {
                recording.put(sessionId, true);
            } else {
                recording.remove(sessionId);
            }
            persistSettingsLocked();
        }
    }

    private void persistSettingsLocked() {
        try {
            Map<String, Boolean> out = new LinkedHashMap<>();
            recording.forEach((key, value) -> out.put(String.valueOf(key), value));
            FileUtils.createDirectory(debugDir());
            FileUtils.writeString(settingsPath(), objectMapper.writeValueAsString(out));
        } catch (Exception e) {
            log.warn("Failed to persist debug settings", e);
        }
    }

    /**
     * 追加一次模型调用记录（仅在开启记录时落盘）。
     *
     * @param sessionId 会话 ID
     * @param inputJson 发送给模型的消息列表 + 工具清单（JSON 字符串，可含 system prompt）
     * @param usageJson 模型用量（JSON 字符串，可为 null）
     */
    public void append(Integer sessionId, String inputJson, String usageJson) {
        if (!isRecording(sessionId)) {
            return;
        }
        try {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("time", Instant.now().toString());
            record.put("input", inputJson != null ? objectMapper.readValue(inputJson, Map.class) : null);
            record.put("usage", usageJson != null ? objectMapper.readValue(usageJson, Map.class) : null);
            FileUtils.createDirectory(debugDir());
            FileUtils.appendString(recordFile(sessionId),
                    objectMapper.writeValueAsString(record) + System.lineSeparator());
        } catch (Exception e) {
            log.warn("Failed to append debug record for session: {}", sessionId, e);
        }
    }

    /** 已记录的模型调用次数（jsonl 行数） */
    public int recordCount(Integer sessionId) {
        if (sessionId == null) {
            return 0;
        }
        Path file = recordFile(sessionId);
        if (!Files.exists(file)) {
            return 0;
        }
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return (int) lines.filter(line -> !line.isBlank()).count();
        } catch (Exception e) {
            log.warn("Failed to count debug records for session: {}", sessionId, e);
            return 0;
        }
    }

    /** 该会话已记录的完整调用列表（按时间先后） */
    public List<Map<String, Object>> listRecords(Integer sessionId) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (sessionId == null) {
            return result;
        }
        Path file = recordFile(sessionId);
        if (!Files.exists(file)) {
            return result;
        }
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            lines.filter(line -> !line.isBlank()).forEach(line -> {
                try {
                    result.add(objectMapper.readValue(line, Map.class));
                } catch (Exception e) {
                    log.warn("Failed to parse debug record line: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("Failed to list debug records for session: {}", sessionId, e);
        }
        return result;
    }
}
