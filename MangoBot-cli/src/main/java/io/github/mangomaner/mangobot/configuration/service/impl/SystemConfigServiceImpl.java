package io.github.mangomaner.mangobot.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.configuration.event.SystemConfigChangedEvent;
import io.github.mangomaner.mangobot.mapper.configuration.SystemConfigMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.SystemConfig;
import io.github.mangomaner.mangobot.configuration.model.dto.system.CreateSystemConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.system.UpdateSystemConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.SystemConfigVO;
import io.github.mangomaner.mangobot.configuration.service.SystemConfigService;
import io.github.mangomaner.mangobot.manager.event.MangoEventPublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Service
@Slf4j
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Resource
    private MangoEventPublisher mangoEventPublisher;

    @Override
    public List<SystemConfigVO> getAllConfigs() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemConfigVO> getConfigsByCategory(String category) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getCategory, category);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigVO getConfigByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = this.getOne(wrapper);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = this.getOne(wrapper);
        if (config == null) {
            return defaultValue;
        }
        return config.getConfigValue() != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public SystemConfigVO createConfig(CreateSystemConfigRequest request) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType() != null ? request.getConfigType() : "STRING");
        config.setDescription(request.getDescription());
        config.setExplain(request.getExplain());
        config.setCategory(request.getCategory() != null ? request.getCategory() : "general");
        config.setEditable(request.getEditable() != null ? (request.getEditable() ? 1 : 0) : 1);
        this.save(config);
        log.info("创建系统配置成功: {}", config.getConfigKey());
        return convertToVO(config);
    }

    @Override
    public SystemConfigVO updateConfig(UpdateSystemConfigRequest request) {
        SystemConfig config = this.getById(request.getId());
        if (config == null) {
            return null;
        }

        String oldKey = config.getConfigKey();
        String oldValue = config.getConfigValue();

        if (request.getConfigKey() != null) {
            config.setConfigKey(request.getConfigKey());
        }
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getExplain() != null) {
            config.setExplain(request.getExplain());
        }
        if (request.getCategory() != null) {
            config.setCategory(request.getCategory());
        }
        if (request.getEditable() != null) {
            config.setEditable(request.getEditable() ? 1 : 0);
        }
        config.setUpdatedAt(System.currentTimeMillis());
        this.updateById(config);

        mangoEventPublisher.publish(new SystemConfigChangedEvent(
                config.getId(),
                config.getConfigKey(),
                config.getConfigType(),
                config.getCategory(),
                oldValue,
                config.getConfigValue()
        ));

        log.info("更新系统配置成功: {}", config.getConfigKey());
        return convertToVO(config);
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = this.getOne(wrapper);
        if (config == null) {
            return false;
        }

        String oldValue = config.getConfigValue();
        config.setConfigValue(configValue);
        config.setUpdatedAt(System.currentTimeMillis());
        boolean result = this.updateById(config);

        if (result) {
            mangoEventPublisher.publish(new SystemConfigChangedEvent(
                    config.getId(),
                    config.getConfigKey(),
                    config.getConfigType(),
                    config.getCategory(),
                    oldValue,
                    configValue
            ));
            log.info("更新系统配置值成功: {} = {}", configKey, configValue);
        }
        return result;
    }

    @Override
    public boolean deleteConfig(Long id) {
        SystemConfig config = this.getById(id);
        if (config == null) {
            return false;
        }
        boolean result = this.removeById(id);
        if (result) {
            log.info("删除系统配置成功: {}", config.getConfigKey());
        }
        return result;
    }

    private SystemConfigVO convertToVO(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(config.getId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
        vo.setDescription(config.getDescription());
        vo.setExplain(config.getExplain());
        vo.setCategory(config.getCategory());
        vo.setEditable(config.getEditable() == 1);
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
