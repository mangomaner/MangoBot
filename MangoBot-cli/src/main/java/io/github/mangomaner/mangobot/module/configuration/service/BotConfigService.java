package io.github.mangomaner.mangobot.module.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.module.configuration.model.domain.BotConfig;
import io.github.mangomaner.mangobot.module.configuration.model.dto.bot.CreateBotConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.bot.UpdateBotConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.BotConfigVO;

import java.util.List;

/**
 * Bot 配置服务接口
 */
public interface BotConfigService extends IService<BotConfig> {

    /**
     * 获取所有 Bot 配置（仅默认配置，bot_id 为 null）
     */
    List<BotConfigVO> getAllConfigs();

    /**
     * 根据 Bot ID 获取配置（懒加载模式：返回默认配置和 Bot 专属配置，优先展示 Bot 专属配置）
     */
    List<BotConfigVO> getConfigsByBotId(String botId);

    /**
     * 根据分类获取配置
     */
    List<BotConfigVO> getConfigsByCategory(String category);

    /**
     * 根据 Key 和 Bot ID 获取配置（优先返回 Bot 专属配置，其次返回默认配置）
     */
    BotConfigVO getConfigByKeyAndBotId(String configKey, String botId);

    /**
     * 根据 Key 获取配置（仅默认配置）
     */
    BotConfigVO getConfigByKey(String configKey);

    /**
     * 根据 Key 和 Bot ID 获取配置值（优先 Bot 专属配置）
     */
    String getConfigValue(String configKey, String botId);

    /**
     * 根据 Key 获取配置值（仅默认配置）
     */
    String getConfigValue(String configKey);

    /**
     * 根据 Key 获取配置值（带默认值，仅默认配置）
     */
    String getConfigValueOrDefault(String configKey, String defaultValue);

    /**
     * 创建 Bot 配置
     */
    BotConfigVO createConfig(CreateBotConfigRequest request);

    /**
     * 更新 Bot 配置（懒加载：若 Bot 专属配置不存在则创建）
     */
    BotConfigVO updateConfig(UpdateBotConfigRequest request);

    /**
     * 根据 Key 和 Bot ID 更新配置值（懒加载：若 Bot 专属配置不存在则创建）
     */
    boolean updateConfigValue(String configKey, String botId, String configValue);

    /**
     * 根据 Key 更新配置值（仅默认配置）
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 删除 Bot 配置
     */
    boolean deleteConfig(Long id);
}
