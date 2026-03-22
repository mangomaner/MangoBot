package io.github.mangomaner.mangobot.adapter.service;

import java.util.List;

/**
 * 平台聊天 API 接口
 * 
 * <p>定义平台无关的聊天操作，不同平台通过实现此接口提供具体功能。
 * 
 * <p>使用策略模式，Controller 根据 platform 参数选择对应的实现。
 */
public interface ChatApiService {

    /**
     * 获取平台类型标识
     * 
     * @return 平台类型 code（如 "onebot_qq"）
     */
    String getPlatformType();

    /**
     * 获取群列表
     * 
     * @param botId 机器人 ID
     * @return 群信息列表
     */
    List<?> getGroupList(long botId);

    /**
     * 获取好友列表
     * 
     * @param botId 机器人 ID
     * @return 好友信息列表
     */
    List<?> getFriendList(long botId);

    /**
     * 发送群消息
     * 
     * @param botId   机器人 ID
     * @param groupId 群 ID
     * @param message 消息内容
     * @return 消息 ID
     */
    Object sendGroupMsg(long botId, long groupId, String message);

    /**
     * 发送私聊消息
     * 
     * @param botId   机器人 ID
     * @param userId  用户 ID
     * @param message 消息内容
     * @return 消息 ID
     */
    Object sendPrivateMsg(long botId, long userId, String message);

    /**
     * 获取登录信息
     * 
     * @param botId 机器人 ID
     * @return 登录信息
     */
    Object getLoginInfo(long botId);
}
