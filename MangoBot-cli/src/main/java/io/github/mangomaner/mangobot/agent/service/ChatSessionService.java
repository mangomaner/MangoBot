package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.agent.model.dto.CreateChatSessionRequest;
import io.github.mangomaner.mangobot.agent.model.dto.UpdateChatSessionRequest;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.agent.model.vo.ChatSessionVO;

import java.util.List;

/**
* @author mangoman
* @description 针对表【chat_session】的数据库操作Service
* @createDate 2026-03-14 23:33:00
*/
public interface ChatSessionService extends IService<ChatSession> {
    /**
     * 创建工作区下的对话会话
     *
     * @param request 创建请求
     * @return 创建的会话视图对象
     */
    ChatSessionVO createSession(CreateChatSessionRequest request);

    /**
     * 根据ID获取会话详情
     *
     * @param id 会话ID
     * @return 会话视图对象
     */
    ChatSessionVO getSessionById(Integer id);

    /**
     * 获取工作区下的所有会话列表
     *
     * @param workspaceId 工作区ID
     * @return 会话视图对象列表
     */
    List<ChatSessionVO> listSessionsByWorkspaceId(Integer workspaceId);

    /**
     * 更新会话信息
     *
     * @param id      会话ID
     * @param request 更新请求
     * @return 更新后的会话视图对象
     */
    ChatSessionVO updateSession(Integer id, UpdateChatSessionRequest request);

    /**
     * 删除会话及其所有消息
     *
     * @param id 会话ID
     */
    void deleteSession(Integer id);

    /**
     * 根据 botId 和 chatId 获取会话详情
     *
     * @param botId  Bot ID
     * @param chatId 群聊ID/私聊ID
     * @return 会话视图对象，如果不存在则返回 null
     */
    ChatSessionVO getSessionByBotIdAndChatId(Long botId, Long chatId, SessionSource source);
}
