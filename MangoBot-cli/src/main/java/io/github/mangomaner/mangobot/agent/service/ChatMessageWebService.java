package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.ChatMessageWeb;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.agent.model.dto.ChatMessageWebRequest;
import io.github.mangomaner.mangobot.agent.model.vo.ChatMessageWebVO;

import java.util.List;

/**
* @author mangoman
* @description 针对表【chat_message_web】的数据库操作Service
* @createDate 2026-03-14 23:33:58
*/
public interface ChatMessageWebService extends IService<ChatMessageWeb> {

    /**
     * 创建对话消息
     *
     * @param request 消息请求
     * @return 创建的消息视图对象
     */
    ChatMessageWebVO createMessage(ChatMessageWebRequest request);

    /**
     * 创建系统消息（用于记录AI回复）
     *
     * @param sessionId 会话ID
     * @param content   消息内容
     * @param metadata  元数据
     * @return 创建的消息视图对象
     */
    ChatMessageWebVO createAssistantMessage(Integer sessionId, String content, String metadata);

    /**
     * 根据ID获取消息
     *
     * @param id 消息ID
     * @return 消息视图对象
     */
    ChatMessageWebVO getMessageById(Integer id);

    /**
     * 获取会话下的所有消息列表（按时间正序）
     *
     * @param sessionId 会话ID
     * @return 消息视图对象列表
     */
    List<ChatMessageWebVO> listMessagesBySessionId(Integer sessionId);

    /**
     * 获取会话下指定角色的消息列表
     *
     * @param sessionId 会话ID
     * @param role      角色（user/assistant/system）
     * @return 消息视图对象列表
     */
    List<ChatMessageWebVO> listMessagesBySessionIdAndRole(Integer sessionId, String role);

    /**
     * 删除会话下的所有消息
     *
     * @param sessionId 会话ID
     */
    void deleteMessagesBySessionId(Integer sessionId);

    /**
     * 删除单条消息
     *
     * @param id 消息ID
     */
    void deleteMessage(Integer id);
}
