package io.github.mangomaner.mangobot.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.configuration.enums.ConfigType;
import io.github.mangomaner.mangobot.configuration.event.SystemConfigChangedEvent;
import io.github.mangomaner.mangobot.configuration.model.config.ConfigMetadata;
import io.github.mangomaner.mangobot.configuration.util.ConfigTypeHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Resource
    private MangoEventPublisher mangoEventPublisher;

    @Resource
    private ConfigTypeHandler configTypeHandler;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<SystemConfigVO> getAllConfigs() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SystemConfig::getBotId);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemConfigVO> getConfigsByBotId(Long botId) {
        if (botId == null) {
            return getAllConfigs();
        }
        
        List<SystemConfigVO> result = new ArrayList<>();
        
        LambdaQueryWrapper<SystemConfig> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.isNull(SystemConfig::getBotId);
        List<SystemConfig> defaultConfigs = this.list(defaultWrapper);
        
        LambdaQueryWrapper<SystemConfig> botWrapper = new LambdaQueryWrapper<>();
        botWrapper.eq(SystemConfig::getBotId, botId);
        List<SystemConfig> botConfigs = this.list(botWrapper);
        
        Map<String, SystemConfig> botConfigMap = botConfigs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, c -> c));
        
        for (SystemConfig defaultConfig : defaultConfigs) {
            SystemConfigVO vo = convertToVO(defaultConfig);
            if (botConfigMap.containsKey(defaultConfig.getConfigKey())) {
                SystemConfig botConfig = botConfigMap.get(defaultConfig.getConfigKey());
                vo = convertToVO(botConfig);
            }
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public List<SystemConfigVO> getConfigsByCategory(String category) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getCategory, category)
               .isNull(SystemConfig::getBotId);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigVO getConfigByKeyAndBotId(String configKey, Long botId) {
        if (botId != null) {
            LambdaQueryWrapper<SystemConfig> botWrapper = new LambdaQueryWrapper<>();
            botWrapper.eq(SystemConfig::getConfigKey, configKey)
                      .eq(SystemConfig::getBotId, botId);
            SystemConfig botConfig = this.getOne(botWrapper);
            if (botConfig != null) {
                return convertToVO(botConfig);
            }
        }
        
        LambdaQueryWrapper<SystemConfig> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.eq(SystemConfig::getConfigKey, configKey)
                      .isNull(SystemConfig::getBotId);
        SystemConfig defaultConfig = this.getOne(defaultWrapper);
        return defaultConfig != null ? convertToVO(defaultConfig) : null;
    }

    @Override
    public SystemConfigVO getConfigByKey(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .isNull(SystemConfig::getBotId);
        SystemConfig config = this.getOne(wrapper);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    public String getConfigValue(String configKey, Long botId) {
        SystemConfigVO config = getConfigByKeyAndBotId(configKey, botId);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, (Long) null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .isNull(SystemConfig::getBotId);
        SystemConfig config = this.getOne(wrapper);
        if (config == null) {
            return defaultValue;
        }
        return config.getConfigValue() != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public SystemConfigVO createConfig(CreateSystemConfigRequest request) {
        ConfigType configType = ConfigType.fromCode(request.getConfigType());
        
        if (request.getConfigValue() != null && !configTypeHandler.validate(configType, request.getConfigValue())) {
            log.warn("配置值验证失败: type={}, value={}", configType, request.getConfigValue());
        }
        
        SystemConfig config = new SystemConfig();
        config.setBotId(request.getBotId());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(configType.getCode());
        config.setMetadata(serializeMetadata(request.getMetadata()));
        config.setDescription(request.getDescription());
        config.setExplain(request.getExplain());
        config.setCategory(request.getCategory() != null ? request.getCategory() : "general");
        config.setEditable(request.getEditable() != null ? (request.getEditable() ? 1 : 0) : 1);
        this.save(config);
        log.info("创建系统配置成功: botId={}, key={}, type={}", config.getBotId(), config.getConfigKey(), configType);
        return convertToVO(config);
    }

    @Override
    public SystemConfigVO updateConfig(UpdateSystemConfigRequest request) {
        SystemConfig config = this.getById(request.getId());
        if (config == null) {
            return null;
        }

        String oldValue = config.getConfigValue();

        if (request.getBotId() != null) {
            config.setBotId(request.getBotId());
        }
        if (request.getConfigKey() != null) {
            config.setConfigKey(request.getConfigKey());
        }
        if (request.getConfigValue() != null) {
            ConfigType configType = ConfigType.fromCode(request.getConfigType() != null ? request.getConfigType() : config.getConfigType());
            if (!configTypeHandler.validate(configType, request.getConfigValue())) {
                log.warn("配置值验证失败: type={}, value={}", configType, request.getConfigValue());
            }
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getMetadata() != null) {
            config.setMetadata(serializeMetadata(request.getMetadata()));
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

        log.info("更新系统配置成功: botId={}, key={}", config.getBotId(), config.getConfigKey());
        return convertToVO(config);
    }

    @Override
    public boolean updateConfigValue(String configKey, Long botId, String configValue) {
        if (botId == null) {
            return updateConfigValue(configKey, configValue);
        }
        
        LambdaQueryWrapper<SystemConfig> botWrapper = new LambdaQueryWrapper<>();
        botWrapper.eq(SystemConfig::getConfigKey, configKey)
                  .eq(SystemConfig::getBotId, botId);
        SystemConfig botConfig = this.getOne(botWrapper);
        
        if (botConfig != null) {
            ConfigType configType = ConfigType.fromCode(botConfig.getConfigType());
            if (!configTypeHandler.validate(configType, configValue)) {
                log.warn("配置值验证失败: type={}, value={}", configType, configValue);
            }
            
            String oldValue = botConfig.getConfigValue();
            botConfig.setConfigValue(configValue);
            botConfig.setUpdatedAt(System.currentTimeMillis());
            boolean result = this.updateById(botConfig);

            if (result) {
                mangoEventPublisher.publish(new SystemConfigChangedEvent(
                        botConfig.getId(),
                        botConfig.getConfigKey(),
                        botConfig.getConfigType(),
                        botConfig.getCategory(),
                        oldValue,
                        configValue
                ));
                log.info("更新系统配置值成功: botId={}, key={}", botId, configKey);
            }
            return result;
        }
        
        LambdaQueryWrapper<SystemConfig> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.eq(SystemConfig::getConfigKey, configKey)
                      .isNull(SystemConfig::getBotId);
        SystemConfig defaultConfig = this.getOne(defaultWrapper);
        
        if (defaultConfig == null) {
            return false;
        }
        
        ConfigType configType = ConfigType.fromCode(defaultConfig.getConfigType());
        if (!configTypeHandler.validate(configType, configValue)) {
            log.warn("配置值验证失败: type={}, value={}", configType, configValue);
        }
        
        SystemConfig newConfig = new SystemConfig();
        newConfig.setBotId(botId);
        newConfig.setConfigKey(configKey);
        newConfig.setConfigValue(configValue);
        newConfig.setConfigType(defaultConfig.getConfigType());
        newConfig.setMetadata(defaultConfig.getMetadata());
        newConfig.setDescription(defaultConfig.getDescription());
        newConfig.setExplain(defaultConfig.getExplain());
        newConfig.setCategory(defaultConfig.getCategory());
        newConfig.setEditable(defaultConfig.getEditable());
        this.save(newConfig);
        
        mangoEventPublisher.publish(new SystemConfigChangedEvent(
                newConfig.getId(),
                newConfig.getConfigKey(),
                newConfig.getConfigType(),
                newConfig.getCategory(),
                defaultConfig.getConfigValue(),
                configValue
        ));
        
        log.info("懒加载创建Bot专属配置: botId={}, key={}", botId, configKey);
        return true;
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .isNull(SystemConfig::getBotId);
        SystemConfig config = this.getOne(wrapper);
        if (config == null) {
            return false;
        }

        ConfigType configType = ConfigType.fromCode(config.getConfigType());
        if (!configTypeHandler.validate(configType, configValue)) {
            log.warn("配置值验证失败: type={}, value={}", configType, configValue);
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
            log.info("更新系统配置值成功: key={}", configKey);
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
            log.info("删除系统配置成功: botId={}, key={}", config.getBotId(), config.getConfigKey());
        }
        return result;
    }

    private SystemConfigVO convertToVO(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        vo.setId(config.getId());
        vo.setBotId(config.getBotId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
        vo.setMetadata(parseMetadata(config.getMetadata()));
        vo.setDescription(config.getDescription());
        vo.setExplain(config.getExplain());
        vo.setCategory(config.getCategory());
        vo.setEditable(config.getEditable() == 1);
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }

    private ConfigMetadata parseMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(metadata, ConfigMetadata.class);
        } catch (JsonProcessingException e) {
            log.warn("解析配置元数据失败: {}", metadata, e);
            return null;
        }
    }

    private String serializeMetadata(ConfigMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("序列化配置元数据失败", e);
            return null;
        }
    }
}
