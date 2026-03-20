package io.github.mangomaner.mangobot.module.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.module.configuration.model.domain.SystemConfig;
import io.github.mangomaner.mangobot.module.configuration.model.dto.system.CreateSystemConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.system.UpdateSystemConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.SystemConfigVO;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 获取所有系统配置（仅默认配置，bot_id 为 null）
     */
    List<SystemConfigVO> getAllConfigs();

    /**
     * 根据Bot ID获取配置（懒加载模式：返回默认配置和Bot专属配置，优先展示Bot专属配置）
     */
    List<SystemConfigVO> getConfigsByBotId(Long botId);

    /**
     * 根据分类获取配置
     */
    List<SystemConfigVO> getConfigsByCategory(String category);

    /**
     * 根据Key和Bot ID获取配置（优先返回Bot专属配置，其次返回默认配置）
     */
    SystemConfigVO getConfigByKeyAndBotId(String configKey, Long botId);

    /**
     * 根据Key获取配置（仅默认配置）
     */
    SystemConfigVO getConfigByKey(String configKey);

    /**
     * 根据Key和Bot ID获取配置值（优先Bot专属配置）
     */
    String getConfigValue(String configKey, Long botId);

    /**
     * 根据Key获取配置值（仅默认配置）
     */
    String getConfigValue(String configKey);

    /**
     * 根据Key获取配置值（带默认值，仅默认配置）
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 创建系统配置
     */
    SystemConfigVO createConfig(CreateSystemConfigRequest request);

    /**
     * 更新系统配置（懒加载：若Bot专属配置不存在则创建）
     */
    SystemConfigVO updateConfig(UpdateSystemConfigRequest request);

    /**
     * 根据Key和Bot ID更新配置值（懒加载：若Bot专属配置不存在则创建）
     */
    boolean updateConfigValue(String configKey, Long botId, String configValue);

    /**
     * 根据Key更新配置值（仅默认配置）
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 删除系统配置
     */
    boolean deleteConfig(Long id);
}
