package io.github.mangomaner.mangobot.agent.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.State;
import io.github.mangomaner.mangobot.agent.model.domain.ChatSession;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.github.mangomaner.mangobot.api.enums.ModelRole;
import io.github.mangomaner.mangobot.mapper.agent.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryManager {

    private final ChatSessionMapper chatSessionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Integer, AutoContextMemory> sessionMemories = new HashMap<>();
    private final Map<Integer, DatabaseSession> sessionStores = new HashMap<>();

    public AutoContextMemory getOrCreateMemory(Integer sessionId) {
        return sessionMemories.computeIfAbsent(sessionId, this::loadOrCreateMemory);
    }

    private AutoContextMemory loadOrCreateMemory(Integer sessionId) {
        OpenAIChatModel model = MangoModelApi.getModel(ModelRole.MAIN);

        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(30)
                .lastKeep(10)
                .tokenRatio(0.3)
                .build();

        AutoContextMemory memory = new AutoContextMemory(config, model);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        String memoryState = session != null ? session.getMemoryState() : null;

        if (memoryState != null && !memoryState.isEmpty()) {
            try {
                DatabaseSession dbSession = new DatabaseSession(memoryState);
                memory.loadIfExists(dbSession, String.valueOf(sessionId));
                sessionStores.put(sessionId, dbSession);
                log.info("Loaded memory state for session: {}", sessionId);
            } catch (Exception e) {
                log.warn("Failed to load memory state for session: {}, creating new memory", sessionId, e);
                sessionStores.put(sessionId, new DatabaseSession());
            }
        } else {
            sessionStores.put(sessionId, new DatabaseSession());
        }

        return memory;
    }

    public void persistMemory(Integer sessionId) {
        AutoContextMemory memory = sessionMemories.get(sessionId);
        DatabaseSession session = sessionStores.get(sessionId);
        
        if (memory == null || session == null) {
            return;
        }

        try {
            memory.saveTo(session, String.valueOf(sessionId));
            String state = session.serialize();
            
            ChatSession chatSession = new ChatSession();
            chatSession.setId(sessionId);
            chatSession.setMemoryState(state);
            chatSession.setUpdateTime(new Date());
            chatSessionMapper.updateById(chatSession);
            log.debug("Persisted memory state for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to persist memory state for session: {}", sessionId, e);
        }
    }

    public void removeMemory(Integer sessionId) {
        sessionMemories.remove(sessionId);
        sessionStores.remove(sessionId);
        log.debug("Removed memory from cache for session: {}", sessionId);
    }

    public void persistAndRemoveMemory(Integer sessionId) {
        persistMemory(sessionId);
        removeMemory(sessionId);
    }

    private static class DatabaseSession implements Session {
        private final Map<String, String> singleValues = new HashMap<>();
        private final Map<String, List<String>> listValues = new HashMap<>();

        DatabaseSession() {}

        DatabaseSession(String serialized) {
            if (serialized != null && !serialized.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> data = mapper.readValue(serialized, Map.class);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, String> singles = (Map<String, String>) data.get("singleValues");
                    if (singles != null) {
                        singleValues.putAll(singles);
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> lists = (Map<String, List<String>>) data.get("listValues");
                    if (lists != null) {
                        listValues.putAll(lists);
                    }
                } catch (Exception e) {
                    log.warn("Failed to deserialize session state", e);
                }
            }
        }

        String serialize() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = new HashMap<>();
                data.put("singleValues", singleValues);
                data.put("listValues", listValues);
                return mapper.writeValueAsString(data);
            } catch (Exception e) {
                log.error("Failed to serialize session state", e);
                return "{}";
            }
        }

        @Override
        public void save(SessionKey sessionKey, String key, State value) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                singleValues.put(key, mapper.writeValueAsString(value));
            } catch (Exception e) {
                log.error("Failed to save state", e);
            }
        }

        @Override
        public void save(SessionKey sessionKey, String key, List<? extends State> values) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> serialized = new ArrayList<>();
                for (State value : values) {
                    serialized.add(mapper.writeValueAsString(value));
                }
                listValues.put(key, serialized);
            } catch (Exception e) {
                log.error("Failed to save state list", e);
            }
        }

        @Override
        public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
            String value = singleValues.get(key);
            if (value == null) {
                return Optional.empty();
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                return Optional.of(mapper.readValue(value, type));
            } catch (Exception e) {
                log.error("Failed to get state", e);
                return Optional.empty();
            }
        }

        @Override
        public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> itemType) {
            List<String> values = listValues.get(key);
            if (values == null) {
                return List.of();
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<T> result = new ArrayList<>();
                for (String value : values) {
                    result.add(mapper.readValue(value, itemType));
                }
                return result;
            } catch (Exception e) {
                log.error("Failed to get state list", e);
                return List.of();
            }
        }

        @Override
        public boolean exists(SessionKey sessionKey) {
            return !singleValues.isEmpty() || !listValues.isEmpty();
        }

        @Override
        public void delete(SessionKey sessionKey) {
            singleValues.clear();
            listValues.clear();
        }

        @Override
        public Set<SessionKey> listSessionKeys() {
            return Set.of();
        }

        @Override
        public void close() {}
    }
}
