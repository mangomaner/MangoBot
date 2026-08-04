package io.github.mangomaner.mangobot.module.agent.service;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.model.vo.PersonaVO;

/**
 * 会话人格提示词 Service
 *
 * <p>会话生效人格 = 定制提示词（chat_session.custom_prompt，非空时） ?: 来源默认人格
 * （AGENTS_GROUP/AGENTS_PRIVATE/AGENTS.md）。生效人格会物化到
 * &lt;workspace&gt;/&lt;userId&gt;/AGENTS.md（会话级覆盖，框架每轮热读）。
 */
public interface SessionPersonaService {

    /**
     * 获取某会话的默认人格、当前定制提示词与生效人格
     */
    PersonaVO getPersona(String botId, String chatId, SessionSource source);

    /**
     * 设置会话定制提示词（null/空白表示清除定制），并重新物化生效人格
     */
    PersonaVO setCustomPrompt(String botId, String chatId, SessionSource source, String customPrompt);

    /**
     * 清除会话定制提示词，恢复来源默认人格
     */
    PersonaVO clearCustomPrompt(String botId, String chatId, SessionSource source);

    /**
     * 物化会话生效人格到 &lt;workspace&gt;/&lt;userId&gt;/AGENTS.md（内容未变则跳过）。
     * 发消息前调用，确保本次对话使用最新人格。
     */
    void materializeSessionPersona(ChatSessionVO session);
}
