package io.github.mangomaner.mangobot.module.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.module.agent.model.domain.ChatMessageWeb;
import io.github.mangomaner.mangobot.module.agent.model.domain.ChatSession;
import io.github.mangomaner.mangobot.module.agent.model.dto.CreateChatSessionRequest;
import io.github.mangomaner.mangobot.module.agent.model.dto.UpdateChatSessionRequest;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.service.ChatMessageWebService;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.github.mangomaner.mangobot.system.exception.BusinessException;
import io.github.mangomaner.mangobot.system.mapper.agent.ChatSessionMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements ChatSessionService {

    @Resource
    private ChatMessageWebService chatMessageService;

    // 【新增】注入自身代理对象，用于解决 @Transactional 自调用失效问题
    // 注意：这里必须注入接口类型 ChatSessionService，而不是实现类
    @Lazy
    @Resource
    private ChatSessionService self;
    /**
     * 状态常量：活跃
     */
    private static final Integer STATUS_ACTIVE = 1;

    /**
     * 状态常量：已归档
     */
    private static final Integer STATUS_ARCHIVED = 0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO createSession(CreateChatSessionRequest request) {
        // 参数校验
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话标题不能为空");
        }

        // 创建会话实体
        ChatSession session = new ChatSession();
        session.setTitle(request.getTitle().trim());
        session.setBotId(request.getBotId());
        session.setChatId(request.getChatId());
        session.setSource(request.getSource() != null ? request.getSource() : SessionSource.WEB);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());

        // 保存到数据库
        boolean saved = this.save(session);
        if (!saved) {
            log.error("创建会话失败: {}", request.getTitle());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建会话失败");
        }

        log.info("创建会话成功，sessionId: {}, title: {}", session.getId(), session.getTitle());
        return convertToVO(session);
    }

    @Override
    public ChatSessionVO getSessionById(Integer id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        ChatSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话不存在");
        }

        return convertToVO(session);
    }

    @Override
    public List<ChatSessionVO> listSessionsByWorkspaceId(Integer workspaceId) {
        if (workspaceId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作区ID不能为空");
        }

        // 查询所有活跃会话，按更新时间倒序
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatSession::getUpdateTime);

        List<ChatSession> sessions = this.list(wrapper);

        return sessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO updateSession(Integer id, UpdateChatSessionRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        // 查询现有会话
        ChatSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话不存在");
        }

        // 更新字段
        boolean needUpdate = false;
        if (StringUtils.hasText(request.getTitle())) {
            session.setTitle(request.getTitle().trim());
            needUpdate = true;
        }

        if (needUpdate) {
            session.setUpdateTime(new Date());
            boolean updated = this.updateById(session);
            if (!updated) {
                log.error("更新会话失败，sessionId: {}", id);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新会话失败");
            }
            log.info("更新会话成功，sessionId: {}", id);
        }

        return convertToVO(session);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Integer id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空");
        }

        ChatSession session = this.getById(id);
        if (session == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话不存在");
        }

        // 先删除会话下的所有消息
        chatMessageService.deleteMessagesBySessionId(id);

        // 删除会话
        boolean removed = this.removeById(id);
        if (!removed) {
            log.error("删除会话失败，sessionId: {}", id);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除会话失败");
        }

        log.info("删除会话成功，sessionId: {}", id);
    }

    @Override
    public ChatSessionVO getSessionByBotIdAndChatId(String botId, String chatId, SessionSource source) {
        if (botId == null || chatId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "botId 和 chatId 不能为空");
        }

        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getBotId, botId)
               .eq(ChatSession::getChatId, chatId)
               .eq(ChatSession::getSource, source);

        ChatSession session = this.getOne(wrapper);

        if (session == null) {
            CreateChatSessionRequest request = CreateChatSessionRequest.builder()
                    .title("群聊" + chatId)
                    .botId(botId)
                    .chatId(chatId)
                    .source(SessionSource.GROUP)
                    .build();
            return self.createSession(request);
        }

        return convertToVO(session);
    }

    /**
     * 将实体转换为视图对象
     *
     * @param session 会话实体
     * @return 会话视图对象
     */
    private ChatSessionVO convertToVO(ChatSession session) {
        if (session == null) {
            return null;
        }

        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);

        // 查询消息数量
        long messageCount = chatMessageService.count(
                new LambdaQueryWrapper<ChatMessageWeb>()
                        .eq(ChatMessageWeb::getSessionId, session.getId())
        );
        vo.setMessageCount(messageCount);

        return vo;
    }
}





