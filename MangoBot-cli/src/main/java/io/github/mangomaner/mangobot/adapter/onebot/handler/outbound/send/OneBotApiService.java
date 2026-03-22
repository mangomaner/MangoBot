package io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.adapter.onebot.handler.echo.OneBotApiResponse;
import io.github.mangomaner.mangobot.adapter.onebot.handler.echo.OneBotEchoHandler;
import io.github.mangomaner.mangobot.adapter.onebot.model.dto.OneBotApiRequest;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.*;
import io.github.mangomaner.mangobot.infra.websocket.ConnectionSessionManager;
import io.github.mangomaner.mangobot.infra.websocket.model.ConnectionSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OneBotApiService {

    private final ConnectionSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final OneBotEchoHandler oneBotEchoHandler;

    public OneBotApiService(ConnectionSessionManager sessionManager, ObjectMapper objectMapper, OneBotEchoHandler oneBotEchoHandler) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.oneBotEchoHandler = oneBotEchoHandler;
    }

    public MessageId sendPrivateMsg(long botId, long userId, OneBotSendingMessage message) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("message", message.getMessage());

        return callApi(botId, "send_private_msg", params, MessageId.class);
    }

    public MessageId sendGroupMsg(long botId, long groupId, OneBotSendingMessage message) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("message", message.getMessage());
        return callApi(botId, "send_group_msg", params, MessageId.class);
    }

    public Void sendGroupPoke(long botId, long groupId, long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        return callApi(botId, "group_poke", params, Void.class);
    }

    public Void sendPrivatePoke(long botId, long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        return callApi(botId, "friend_poke", params, Void.class);
    }

    public void deleteMsg(long botId, int messageId) {
        Map<String, Object> params = new HashMap<>();
        params.put("message_id", messageId);
        callApiVoid(botId, "delete_msg", params);
    }

    public MessageId sendGroupForwardMsg(long botId, long groupId, Object messages) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("messages", messages);
        return callApi(botId, "send_group_forward_msg", params, MessageId.class);
    }

    public MessageId sendPrivateForwardMsg(long botId, long userId, Object messages) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("messages", messages);
        return callApi(botId, "send_private_forward_msg", params, MessageId.class);
    }

    public GroupInfo getGroupInfo(long botId, long groupId, boolean noCache) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("no_cache", noCache);
        return callApi(botId, "get_group_info", params, GroupInfo.class);
    }

    @Deprecated
    public MessageId sendMsg(long botId, String messageType, Long userId, Long groupId, Object message) {
        Map<String, Object> params = new HashMap<>();
        params.put("message_type", messageType);
        if (userId != null) params.put("user_id", userId);
        if (groupId != null) params.put("group_id", groupId);
        params.put("message", message);
        return callApi(botId, "send_msg", params, MessageId.class);
    }

    public MessageInfo getMsg(long botId, int messageId) {
        Map<String, Object> params = new HashMap<>();
        params.put("message_id", messageId);
        return callApi(botId, "get_msg", params, MessageInfo.class);
    }

    public List<OneBotGroupMessageEvent> getForwardMsg(long botId, String id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        Map<String, Object> apiResult = callApi(botId, "get_forward_msg", params, Map.class);
        List<Map<String, Object>> result = (List<Map<String, Object>>) apiResult.get("messages");

        List<OneBotGroupMessageEvent> resultList = new ArrayList<>();
        for (Map<String, Object> item : result) {
            try {
                Map<String, Object> modifiedItem = new HashMap<>(item);
                if (modifiedItem.containsKey("content")) {
                    modifiedItem.put("message", modifiedItem.get("content"));
                }
                OneBotGroupMessageEvent event = objectMapper.convertValue(modifiedItem, OneBotGroupMessageEvent.class);
                resultList.add(event);
            } catch (Exception e) {
                log.error("Failed to parse forward message: {}", item, e);
            }
        }
        return resultList;
    }

    public void sendLike(long botId, long userId, int times) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("times", times);
        callApiVoid(botId, "send_like", params);
    }

    public void setGroupKick(long botId, long groupId, long userId, boolean rejectAddRequest) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("reject_add_request", rejectAddRequest);
        callApiVoid(botId, "set_group_kick", params);
    }

    public void setGroupBan(long botId, long groupId, long userId, long duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("duration", duration);
        callApiVoid(botId, "set_group_ban", params);
    }

    public void setGroupWholeBan(long botId, long groupId, boolean enable) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("enable", enable);
        callApiVoid(botId, "set_group_whole_ban", params);
    }

    public void setGroupAdmin(long botId, long groupId, long userId, boolean enable) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("enable", enable);
        callApiVoid(botId, "set_group_admin", params);
    }

    public void setGroupAnonymous(long botId, long groupId, boolean enable) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("enable", enable);
        callApiVoid(botId, "set_group_anonymous", params);
    }

    public void setGroupCard(long botId, long groupId, long userId, String card) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("card", card);
        callApiVoid(botId, "set_group_card", params);
    }

    public void setGroupName(long botId, long groupId, String groupName) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("group_name", groupName);
        callApiVoid(botId, "set_group_name", params);
    }

    public void setGroupLeave(long botId, long groupId, boolean isDismiss) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("is_dismiss", isDismiss);
        callApiVoid(botId, "set_group_leave", params);
    }

    public void setGroupSpecialTitle(long botId, long groupId, long userId, String specialTitle, long duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("special_title", specialTitle);
        params.put("duration", duration);
        callApiVoid(botId, "set_group_special_title", params);
    }

    public void setFriendAddRequest(long botId, String flag, boolean approve, String remark) {
        Map<String, Object> params = new HashMap<>();
        params.put("flag", flag);
        params.put("approve", approve);
        params.put("remark", remark);
        callApiVoid(botId, "set_friend_add_request", params);
    }

    public void setGroupAddRequest(long botId, String flag, String subType, boolean approve, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("flag", flag);
        params.put("sub_type", subType);
        params.put("approve", approve);
        params.put("reason", reason);
        callApiVoid(botId, "set_group_add_request", params);
    }

    public LoginInfo getLoginInfo(long botId) {
        return callApi(botId, "get_login_info", new HashMap<>(), LoginInfo.class);
    }

    public StrangerInfo getStrangerInfo(long botId, long userId, boolean noCache) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("no_cache", noCache);
        return callApi(botId, "get_stranger_info", params, StrangerInfo.class);
    }

    public List<FriendInfo> getFriendList(long botId) {
        return callApiList(botId, "get_friend_list", new HashMap<>(), FriendInfo.class);
    }

    public List<GroupInfo> getGroupList(long botId) {
        return callApiList(botId, "get_group_list", new HashMap<>(), GroupInfo.class);
    }

    public GroupMemberInfo getGroupMemberInfo(long botId, long groupId, long userId, boolean noCache) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("no_cache", noCache);
        return callApi(botId, "get_group_member_info", params, GroupMemberInfo.class);
    }

    public List<GroupMemberInfo> getGroupMemberList(long botId, long groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        return callApiList(botId, "get_group_member_list", params, GroupMemberInfo.class);
    }

    public GroupHonorInfo getGroupHonorInfo(long botId, long groupId, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        params.put("type", type);
        return callApi(botId, "get_group_honor_info", params, GroupHonorInfo.class);
    }

    public FileInfo getRecord(long botId, String file, String outFormat) {
        Map<String, Object> params = new HashMap<>();
        params.put("file", file);
        params.put("out_format", outFormat);
        return callApi(botId, "get_record", params, FileInfo.class);
    }

    public FileInfo getImage(long botId, String file) {
        Map<String, Object> params = new HashMap<>();
        params.put("file", file);
        return callApi(botId, "get_image", params, FileInfo.class);
    }

    public CanSendInfo canSendImage(long botId) {
        return callApi(botId, "can_send_image", new HashMap<>(), CanSendInfo.class);
    }

    public CanSendInfo canSendRecord(long botId) {
        return callApi(botId, "can_send_record", new HashMap<>(), CanSendInfo.class);
    }

    public void callApiVoid(long botId, String action, Map<String, Object> params) {
        callApi(botId, action, params, Void.class);
    }

    public <T> List<T> callApiList(long botId, String action, Map<String, Object> params, Class<T> elementType) {
        Object data = callApiRaw(botId, action, params);
        if (data == null) {
            return Collections.emptyList();
        }
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return objectMapper.convertValue(data, listType);
    }

    public <T> T callApi(long botId, String action, Map<String, Object> params, Class<T> responseType) {
        Object data = callApiRaw(botId, action, params);
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, responseType);
    }

    private Object callApiRaw(long botId, String action, Map<String, Object> params) {
        ConnectionSession session = sessionManager.getSessionBySelfId(botId);
        if (session == null || !session.isConnected()) {
            log.error("机器人 {} 未连接或会话不存在，无法发送 API 请求: {}", botId, action);
            return null;
        }

        String echo = UUID.randomUUID().toString();
        OneBotApiRequest request = new OneBotApiRequest();
        request.setAction(action);
        request.setParams(params);
        request.setEcho(echo);

        try {
            oneBotEchoHandler.register(echo);
            
            String json = objectMapper.writeValueAsString(request);
            log.debug("发送 API 请求 [{}]: {}", action, json);

            WebSocket connection = session.getConnection();
            synchronized (connection) {
                connection.send(json);
            }
            
            OneBotApiResponse response = oneBotEchoHandler.waitForResponse(echo, 60, TimeUnit.SECONDS);
            
            if (response.getRetcode() != 0) {
                log.warn("API 调用返回非零状态: {} - {}", response.getRetcode(), response.getMessage());
            }
            
            return response.getData();
            
        } catch (Exception e) {
            log.error("发送 API 请求失败: {}", action, e);
            return null;
        }
    }
}
