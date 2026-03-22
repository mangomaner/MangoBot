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
     * 获取所有系统配置
     */
    List<SystemConfigVO> getAllConfigs();

    /**
     * 根据分类获取配置
     */
    List<SystemConfigVO> getConfigsByCategory(String category);

    /**
     * 根据 Key 获取配置
     */
    SystemConfigVO getConfigByKey(String configKey);

    /**
     * 根据 Key 获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据 Key 获取配置值（带默认值）
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 创建系统配置
     */
    SystemConfigVO createConfig(CreateSystemConfigRequest request);

    /**
     * 更新系统配置
     */
    SystemConfigVO updateConfig(UpdateSystemConfigRequest request);

    /**
     * 根据 Key 更新配置值
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 删除系统配置
     */
    boolean deleteConfig(Long id);
}
