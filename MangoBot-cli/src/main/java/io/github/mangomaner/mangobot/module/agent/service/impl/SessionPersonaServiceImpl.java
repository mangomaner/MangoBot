package io.github.mangomaner.mangobot.module.agent.service.impl;

import io.github.mangomaner.mangobot.module.agent.core.AgentWorkspaceManager;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.model.vo.PersonaVO;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.module.agent.service.SessionPersonaService;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.github.mangomaner.mangobot.system.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 会话人格提示词 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPersonaServiceImpl implements SessionPersonaService {

    private final AgentWorkspaceManager agentWorkspaceManager;
    private final ChatSessionService chatSessionService;

    @Override
    public PersonaVO getPersona(String botId, String chatId, SessionSource source) {
        validate(botId, chatId, source);
        String defaultPrompt = agentWorkspaceManager.readDefaultPersona(botId, source);
        ChatSessionVO session = chatSessionService.getSessionByBotIdAndChatIdOrNull(botId, chatId, source);
        String customPrompt = session != null ? session.getCustomPrompt() : null;
        return buildVO(botId, chatId, source, defaultPrompt, customPrompt);
    }

    @Override
    public PersonaVO setCustomPrompt(String botId, String chatId, SessionSource source, String customPrompt) {
        validate(botId, chatId, source);
        ChatSessionVO session = chatSessionService.getSessionByBotIdAndChatId(botId, chatId, source);
        chatSessionService.updateCustomPrompt(session.getId(), customPrompt);
        ChatSessionVO updated = chatSessionService.getSessionById(session.getId());
        materializeSessionPersona(updated);
        return getPersona(botId, chatId, source);
    }

    @Override
    public PersonaVO clearCustomPrompt(String botId, String chatId, SessionSource source) {
        return setCustomPrompt(botId, chatId, source, null);
    }

    @Override
    public void materializeSessionPersona(ChatSessionVO session) {
        if (session == null || session.getSource() == null) {
            return;
        }
        if (StringUtils.hasText(session.getCustomPrompt())) {
            // 定制人格：写入会话级 AGENTS.md，覆盖该来源默认人格
            agentWorkspaceManager.writeSessionAgentsMdIfChanged(
                    session.getBotId(), session.getSource(), session.getChatId(), session.getCustomPrompt());
        } else {
            // 默认人格：删除会话级 AGENTS.md，回退到来源工作区默认文件（随默认改动热生效）
            agentWorkspaceManager.deleteSessionAgentsMd(
                    session.getBotId(), session.getSource(), session.getChatId());
        }
    }

    private PersonaVO buildVO(String botId, String chatId, SessionSource source,
                              String defaultPrompt, String customPrompt) {
        return PersonaVO.builder()
                .botId(botId)
                .chatId(chatId)
                .source(source)
                .defaultPrompt(defaultPrompt)
                .customPrompt(customPrompt)
                .hasCustomPrompt(StringUtils.hasText(customPrompt))
                .build();
    }

    private void validate(String botId, String chatId, SessionSource source) {
        if (source == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "source 不能为空");
        }
        if (botId == null || chatId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "botId/chatId 不能为空");
        }
    }
}
