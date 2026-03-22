package io.github.mangomaner.mangobot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send.OneBotApiService;
import io.github.mangomaner.mangobot.adapter.onebot.utils.OneBotMessageParser;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.*;
import io.github.mangomaner.mangobot.module.message.groupMessage.service.GroupMessagesService;
import io.github.mangomaner.mangobot.module.message.model.domain.GroupMessages;
import io.github.mangomaner.mangobot.module.message.model.domain.PrivateMessages;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;

import java.util.List;
import java.util.Map;

/**
 * OneBot API (静态工具类)
 * 提供对 OneBot 协议的 API 调用能力，包括发送消息、群管理、获取信息等。
 */
public class MangoOneBotApi {

    private static OneBotApiService service;
    private static GroupMessagesService groupMessagesService;
    private static PrivateMessagesService privateMessagesService;
    private static OneBotMessageParser messageParser;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MangoOneBotApi() {}

    static void setService(OneBotApiService service) {
        MangoOneBotApi.service = service;
    }

    static void setGroupMessagesService(GroupMessagesService groupMessagesService) {
        MangoOneBotApi.groupMessagesService = groupMessagesService;
    }

    static void setPrivateMessagesService(PrivateMessagesService privateMessagesService) {
        MangoOneBotApi.privateMessagesService = privateMessagesService;
    }

    static void setMessageParser(OneBotMessageParser messageParser) {
        MangoOneBotApi.messageParser = messageParser;
    }

    private static void checkService() {
        if (service == null) {
            throw new IllegalStateException("MangoOneBotApi has not been initialized yet.");
        }
    }

    /**
     * 发送私聊消息
     *
     * @param botId  机器人QQ号
     * @param userId 对方QQ号
     * @param message 消息内容
     * @return MessageId
     */
    public static MessageId sendPrivateMsg(long botId, long userId, OneBotSendingMessage message) {
        checkService();
        MessageId result = service.sendPrivateMsg(botId, userId, message);
        if (privateMessagesService != null && messageParser != null) {
            try {
                PrivateMessages privateMessages = new PrivateMessages();
                privateMessages.setBotId(String.valueOf(botId));
                privateMessages.setFriendId(String.valueOf(userId));
                privateMessages.setMessageId(String.valueOf(result.getMessageId()));
                privateMessages.setSenderId(String.valueOf(botId));
                privateMessages.setMessageSegments(objectMapper.writeValueAsString(message.getMessage()));
                privateMessages.setMessageTime(System.currentTimeMillis());
                privateMessages.setParseMessage(messageParser.parseMessage(message.getMessage(), botId));
                privateMessagesService.save(privateMessages);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save private message", e);
            }
        }
        return result;
    }

    /**
     * 发送群消息
     *
     * @param botId   机器人QQ号
     * @param groupId 群号
     * @param message 消息内容
     * @return MessageId
     */
    public static MessageId sendGroupMsg(long botId, long groupId, OneBotSendingMessage message) {
        checkService();
        MessageId result = service.sendGroupMsg(botId, groupId, message);
        if (groupMessagesService != null && messageParser != null) {
            try {
                GroupMessages groupMessages = new GroupMessages();
                groupMessages.setBotId(String.valueOf(botId));
                groupMessages.setGroupId(String.valueOf(groupId));
                groupMessages.setMessageId(String.valueOf(result.getMessageId()));
                groupMessages.setSenderId(String.valueOf(botId));
                groupMessages.setMessageSegments(objectMapper.writeValueAsString(message.getMessage()));
                groupMessages.setMessageTime(System.currentTimeMillis());
                groupMessages.setParseMessage(messageParser.parseMessage(message.getMessage(), botId));
                groupMessagesService.addGroupMessage(groupMessages);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save group message", e);
            }
        }
        return result;
    }

    /**
     * 群组戳一戳
     */
    public static Void sendGroupPoke(long botId, long groupId, long userId) {
        checkService();
        return service.sendGroupPoke(botId, groupId, userId);
    }

    /**
     * 私聊戳一戳
     */
    public static Void sendPrivatePoke(long botId, long userId) {
        checkService();
        return service.sendPrivatePoke(botId, userId);
    }

    /**
     * 撤回消息
     *
     * @param botId     机器人QQ号
     * @param messageId 消息ID
     */
    public static void deleteMsg(long botId, int messageId) {
        checkService();
        service.deleteMsg(botId, messageId);
    }

    /**
     * 发送合并转发消息 (群)
     *
     * @param botId 机器人QQ号
     * @param groupId 群号
     * @param messages 消息节点列表
     */
    public static MessageId sendGroupForwardMsg(long botId, long groupId, Object messages) {
        checkService();
        return service.sendGroupForwardMsg(botId, groupId, messages);
    }

    /**
     * 发送合并转发消息 (私聊)
     *
     * @param botId 机器人QQ号
     * @param userId 用户QQ号
     * @param messages 消息节点列表
     */
    public static MessageId sendPrivateForwardMsg(long botId, long userId, Object messages) {
        checkService();
        return service.sendPrivateForwardMsg(botId, userId, messages);
    }

    /**
     * 获取群信息
     *
     * @param botId   机器人QQ号
     * @param groupId 群号
     * @param noCache 是否不使用缓存
     */
    public static GroupInfo getGroupInfo(long botId, long groupId, boolean noCache) {
        checkService();
        return service.getGroupInfo(botId, groupId, noCache);
    }

    /**
     * 发送消息 (通用)
     */
    @Deprecated
    public static MessageId sendMsg(long botId, String messageType, Long userId, Long groupId, Object message) {
        checkService();
        return service.sendMsg(botId, messageType, userId, groupId, message);
    }

    /**
     * 获取消息
     */
    public static MessageInfo getMsg(long botId, int messageId) {
        checkService();
        return service.getMsg(botId, messageId);
    }

    /**
     * 获取合并转发消息
     */
    public static List<OneBotGroupMessageEvent> getForwardMsg(long botId, String id) {
        checkService();
        return service.getForwardMsg(botId, id);
    }

    /**
     * 发送好友赞
     */
    public static void sendLike(long botId, long userId, int times) {
        checkService();
        service.sendLike(botId, userId, times);
    }

    /**
     * 群组踢人
     */
    public static void setGroupKick(long botId, long groupId, long userId, boolean rejectAddRequest) {
        checkService();
        service.setGroupKick(botId, groupId, userId, rejectAddRequest);
    }

    /**
     * 群组单人禁言
     */
    public static void setGroupBan(long botId, long groupId, long userId, long duration) {
        checkService();
        service.setGroupBan(botId, groupId, userId, duration);
    }

    /**
     * 群组全员禁言
     */
    public static void setGroupWholeBan(long botId, long groupId, boolean enable) {
        checkService();
        service.setGroupWholeBan(botId, groupId, enable);
    }

    /**
     * 群组设置管理员
     */
    public static void setGroupAdmin(long botId, long groupId, long userId, boolean enable) {
        checkService();
        service.setGroupAdmin(botId, groupId, userId, enable);
    }

    /**
     * 群组匿名
     */
    public static void setGroupAnonymous(long botId, long groupId, boolean enable) {
        checkService();
        service.setGroupAnonymous(botId, groupId, enable);
    }

    /**
     * 设置群名片
     */
    public static void setGroupCard(long botId, long groupId, long userId, String card) {
        checkService();
        service.setGroupCard(botId, groupId, userId, card);
    }

    /**
     * 设置群名
     */
    public static void setGroupName(long botId, long groupId, String groupName) {
        checkService();
        service.setGroupName(botId, groupId, groupName);
    }

    /**
     * 退出群组
     */
    public static void setGroupLeave(long botId, long groupId, boolean isDismiss) {
        checkService();
        service.setGroupLeave(botId, groupId, isDismiss);
    }

    /**
     * 设置群组专属头衔
     */
    public static void setGroupSpecialTitle(long botId, long groupId, long userId, String specialTitle, long duration) {
        checkService();
        service.setGroupSpecialTitle(botId, groupId, userId, specialTitle, duration);
    }

    /**
     * 处理加好友请求
     */
    public static void setFriendAddRequest(long botId, String flag, boolean approve, String remark) {
        checkService();
        service.setFriendAddRequest(botId, flag, approve, remark);
    }

    /**
     * 处理加群请求／邀请
     */
    public static void setGroupAddRequest(long botId, String flag, String subType, boolean approve, String reason) {
        checkService();
        service.setGroupAddRequest(botId, flag, subType, approve, reason);
    }

    /**
     * 获取登录号信息
     */
    public static LoginInfo getLoginInfo(long botId) {
        checkService();
        return service.getLoginInfo(botId);
    }

    /**
     * 获取陌生人信息
     */
    public static StrangerInfo getStrangerInfo(long botId, long userId, boolean noCache) {
        checkService();
        return service.getStrangerInfo(botId, userId, noCache);
    }

    /**
     * 获取好友列表
     */
    public static List<FriendInfo> getFriendList(long botId) {
        checkService();
        return service.getFriendList(botId);
    }

    /**
     * 获取群列表
     */
    public static List<GroupInfo> getGroupList(long botId) {
        checkService();
        return service.getGroupList(botId);
    }

    /**
     * 获取群成员信息
     */
    public static GroupMemberInfo getGroupMemberInfo(long botId, long groupId, long userId, boolean noCache) {
        checkService();
        return service.getGroupMemberInfo(botId, groupId, userId, noCache);
    }

    /**
     * 获取群成员列表
     */
    public static List<GroupMemberInfo> getGroupMemberList(long botId, long groupId) {
        checkService();
        return service.getGroupMemberList(botId, groupId);
    }

    /**
     * 获取群荣誉信息
     */
    public static GroupHonorInfo getGroupHonorInfo(long botId, long groupId, String type) {
        checkService();
        return service.getGroupHonorInfo(botId, groupId, type);
    }

    /**
     * 获取语音
     *
     * @param file 收到的语音文件名
     * @param outFormat 要转换到的格式
     * @return FileInfo
     */
    public static FileInfo getRecord(long botId, String file, String outFormat) {
        checkService();
        return service.getRecord(botId, file, outFormat);
    }

    /**
     * 获取图片
     *
     * @param file 收到的图片文件名
     * @return FileInfo
     */
    public static FileInfo getImage(long botId, String file) {
        checkService();
        return service.getImage(botId, file);
    }

    /**
     * 检查是否可以发送图片
     */
    public static CanSendInfo canSendImage(long botId) {
        checkService();
        return service.canSendImage(botId);
    }

    /**
     * 检查是否可以发送语音
     */
    public static CanSendInfo canSendRecord(long botId) {
        checkService();
        return service.canSendRecord(botId);
    }

    /**
     * 通用 API 调用方法 (返回 void)
     */
    public static void callApiVoid(long botId, String action, Map<String, Object> params) {
        checkService();
        service.callApiVoid(botId, action, params);
    }

    /**
     * 通用 API 调用方法 (返回 List)
     */
    public static <T> List<T> callApiList(long botId, String action, Map<String, Object> params, Class<T> elementType) {
        checkService();
        return service.callApiList(botId, action, params, elementType);
    }

    /**
     * 通用 API 调用方法 (返回指定类型)
     */
    public static <T> T callApi(long botId, String action, Map<String, Object> params, Class<T> responseType) {
        checkService();
        return service.callApi(botId, action, params, responseType);
    }
}
