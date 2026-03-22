package io.github.mangomaner.mangobot.module.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.module.configuration.enums.ConfigType;
import io.github.mangomaner.mangobot.module.configuration.event.PluginConfigChangedEvent;
import io.github.mangomaner.mangobot.module.configuration.model.config.ConfigMetadata;
import io.github.mangomaner.mangobot.module.configuration.model.config.PluginConfigDefinition;
import io.github.mangomaner.mangobot.module.configuration.util.ConfigTypeHandler;
import io.github.mangomaner.mangobot.system.mapper.configuration.PluginConfigMapper;
import io.github.mangomaner.mangobot.module.configuration.model.domain.PluginConfigEntity;
import io.github.mangomaner.mangobot.module.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.PluginConfigVO;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.infra.MangoEventPublisher;
import io.github.mangomaner.mangobot.plugin.model.domain.Plugins;
import io.github.mangomaner.mangobot.plugin.service.PluginsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PluginConfigServiceImpl extends ServiceImpl<PluginConfigMapper, PluginConfigEntity>
        implements PluginConfigService {

    @Resource
    private MangoEventPublisher mangoEventPublisher;

    @Resource
    private PluginsService pluginsService;

    @Resource
    private ConfigTypeHandler configTypeHandler;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<PluginConfigVO> getAllConfigs() {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(PluginConfigEntity::getBotId);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginConfigVO> getConfigsByBotId(String botId) {
        if (botId == null) {
            return getAllConfigs();
        }
        
        List<PluginConfigVO> result = new ArrayList<>();
        
        LambdaQueryWrapper<PluginConfigEntity> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.isNull(PluginConfigEntity::getBotId);
        List<PluginConfigEntity> defaultConfigs = this.list(defaultWrapper);
        
        LambdaQueryWrapper<PluginConfigEntity> botWrapper = new LambdaQueryWrapper<>();
        botWrapper.eq(PluginConfigEntity::getBotId, botId);
        List<PluginConfigEntity> botConfigs = this.list(botWrapper);
        
        Map<String, PluginConfigEntity> botConfigMap = botConfigs.stream()
                .collect(Collectors.toMap(c -> c.getPluginId() + "_" + c.getConfigKey(), c -> c));
        
        for (PluginConfigEntity defaultConfig : defaultConfigs) {
            String key = defaultConfig.getPluginId() + "_" + defaultConfig.getConfigKey();
            PluginConfigVO vo;
            if (botConfigMap.containsKey(key)) {
                vo = convertToVO(botConfigMap.get(key));
            } else {
                vo = convertToVO(defaultConfig);
            }
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public List<PluginConfigVO> getConfigsByPluginId(Long pluginId) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
               .isNull(PluginConfigEntity::getBotId);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginConfigVO> getConfigsByPluginIdAndBotId(Long pluginId, String botId) {
        if (botId == null) {
            return getConfigsByPluginId(pluginId);
        }
        
        List<PluginConfigVO> result = new ArrayList<>();
        
        LambdaQueryWrapper<PluginConfigEntity> defaultWrapper = new LambdaQueryWrapper<>();
        defaultWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                      .isNull(PluginConfigEntity::getBotId);
        List<PluginConfigEntity> defaultConfigs = this.list(defaultWrapper);
        
        LambdaQueryWrapper<PluginConfigEntity> botWrapper = new LambdaQueryWrapper<>();
        botWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                  .eq(PluginConfigEntity::getBotId, botId);
        List<PluginConfigEntity> botConfigs = this.list(botWrapper);
        
        Map<String, PluginConfigEntity> botConfigMap = botConfigs.stream()
                .collect(Collectors.toMap(PluginConfigEntity::getConfigKey, c -> c));
        
        for (PluginConfigEntity defaultConfig : defaultConfigs) {
            PluginConfigVO vo;
            if (botConfigMap.containsKey(defaultConfig.getConfigKey())) {
                vo = convertToVO(botConfigMap.get(defaultConfig.getConfigKey()));
            } else {
                vo = convertToVO(defaultConfig);
            }
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public PluginConfigVO getConfig(Long pluginId, String configKey) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey)
                .isNull(PluginConfigEntity::getBotId);
        PluginConfigEntity config = this.getOne(wrapper);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    public PluginConfigVO getConfig(Long pluginId, String botId, String configKey) {
        if (botId != null) {
            LambdaQueryWrapper<PluginConfigEntity> botWrapper = new LambdaQueryWrapper<>();
            botWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                      .eq(PluginConfigEntity::getConfigKey, configKey)
                      .eq(PluginConfigEntity::getBotId, botId);
            PluginConfigEntity botConfig = this.getOne(botWrapper);
            if (botConfig != null) {
                return convertToVO(botConfig);
            }
        }
        return getConfig(pluginId, configKey);
    }

    @Override
    public String getConfigValue(Long pluginId, String configKey) {
        return getConfigValueByBotId(pluginId, null, configKey);
    }

    @Override
    public String getConfigValueByBotId(Long pluginId, String botId, String configKey) {
        PluginConfigVO config = getConfig(pluginId, botId, configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValueOrDefault(Long pluginId, String configKey, String defaultValue) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey)
                .isNull(PluginConfigEntity::getBotId);
        PluginConfigEntity config = this.getOne(wrapper);
        if (config == null) {
            return defaultValue;
        }
        return config.getConfigValue() != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public void registerConfig(Long pluginId, String configKey, String configValue,
                                String configType, String description, String explain) {
        ConfigType type = ConfigType.fromCode(configType);
        
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey)
                .isNull(PluginConfigEntity::getBotId);
        PluginConfigEntity existing = this.getOne(wrapper);

        if (existing != null) {
            existing.setConfigValue(configValue);
            existing.setDescription(description);
            existing.setExplain(explain);
            existing.setUpdatedAt(System.currentTimeMillis());
            this.updateById(existing);
            log.debug("更新插件配置: pluginId={}, key={}", pluginId, configKey);
        } else {
            PluginConfigEntity config = new PluginConfigEntity();
            config.setPluginId(pluginId);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType(type.getCode());
            config.setDescription(description);
            config.setExplain(explain);
            config.setEditable(1);
            this.save(config);
            log.debug("注册插件配置: pluginId={}, key={}, type={}", pluginId, configKey, type);
        }
    }

    @Override
    public void registerDefinition(Long pluginId, PluginConfigDefinition definition) {
        if (definition == null) {
            return;
        }
        
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, definition.getKey())
                .isNull(PluginConfigEntity::getBotId);
        PluginConfigEntity existing = this.getOne(wrapper);

        String metadataJson = serializeMetadata(definition.getMetadata());
        
        if (existing != null) {
            existing.setConfigValue(definition.getValue());
            existing.setConfigType(definition.getType().getCode());
            existing.setDescription(definition.getDescription());
            existing.setExplain(definition.getExplain());
            existing.setMetadata(metadataJson);
            existing.setEditable(definition.getEditable() ? 1 : 0);
            existing.setUpdatedAt(System.currentTimeMillis());
            this.updateById(existing);
            log.debug("更新插件配置定义: pluginId={}, key={}", pluginId, definition.getKey());
        } else {
            PluginConfigEntity config = new PluginConfigEntity();
            config.setPluginId(pluginId);
            config.setConfigKey(definition.getKey());
            config.setConfigValue(definition.getValue());
            config.setConfigType(definition.getType().getCode());
            config.setDescription(definition.getDescription());
            config.setExplain(definition.getExplain());
            config.setMetadata(metadataJson);
            config.setEditable(definition.getEditable() ? 1 : 0);
            this.save(config);
            log.debug("注册插件配置定义: pluginId={}, key={}, type={}", pluginId, definition.getKey(), definition.getType());
        }
    }

    @Override
    public void registerDefinitions(Long pluginId, List<PluginConfigDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }
        for (PluginConfigDefinition definition : definitions) {
            registerDefinition(pluginId, definition);
        }
        log.info("批量注册插件配置: pluginId={}, count={}", pluginId, definitions.size());
    }

    @Override
    public PluginConfigVO updateConfig(UpdatePluginConfigRequest request) {
        PluginConfigEntity config = this.getById(request.getId());
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
        config.setUpdatedAt(System.currentTimeMillis());
        this.updateById(config);

        Plugins plugin = pluginsService.getById(config.getPluginId());
        String pluginName = plugin != null ? plugin.getPluginName() : "unknown";

        mangoEventPublisher.publish(new PluginConfigChangedEvent(
                config.getId(),
                config.getPluginId(),
                pluginName,
                config.getConfigKey(),
                config.getConfigType(),
                oldValue,
                config.getConfigValue()
        ));

        log.info("更新插件配置成功: pluginId={}, botId={}, key={}", config.getPluginId(), config.getBotId(), config.getConfigKey());
        return convertToVO(config);
    }

    @Override
    public boolean updateConfigValue(Long pluginId, String configKey, String configValue) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey)
                .isNull(PluginConfigEntity::getBotId);
        PluginConfigEntity config = this.getOne(wrapper);
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
            Plugins plugin = pluginsService.getById(pluginId);
            String pluginName = plugin != null ? plugin.getPluginName() : "unknown";

            mangoEventPublisher.publish(new PluginConfigChangedEvent(
                    config.getId(),
                    pluginId,
                    pluginName,
                    configKey,
                    config.getConfigType(),
                    oldValue,
                    configValue
            ));
            log.info("更新插件配置值成功: pluginId={}, key={}", pluginId, configKey);
        }
        return result;
    }

    @Override
    public boolean updateConfigValue(Long pluginId, String botId, String configKey, String configValue) {
        if (botId == null) {
            return updateConfigValue(pluginId, configKey, configValue);
        }
        
        LambdaQueryWrapper<PluginConfigEntity> botWrapper = new LambdaQueryWrapper<>();
        botWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                  .eq(PluginConfigEntity::getConfigKey, configKey)
                  .eq(PluginConfigEntity::getBotId, botId);
        PluginConfigEntity botConfig = this.getOne(botWrapper);
        
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
                Plugins plugin = pluginsService.getById(pluginId);
                String pluginName = plugin != null ? plugin.getPluginName() : "unknown";

                mangoEventPublisher.publish(new PluginConfigChangedEvent(
                        botConfig.getId(),
                        pluginId,
                        pluginName,
                        configKey,
                        botConfig.getConfigType(),
                        oldValue,
                        configValue
                ));
                log.info("更新插件配置值成功: pluginId={}, botId={}, key={}", pluginId, botId, configKey);
            }
            return result;
        }
        
        LambdaQueryWrapper<PluginConfigEntity> checkBotWrapper = new LambdaQueryWrapper<>();
        checkBotWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                       .eq(PluginConfigEntity::getBotId, botId);
        long botConfigCount = this.count(checkBotWrapper);
        
        if (botConfigCount == 0) {
            LambdaQueryWrapper<PluginConfigEntity> defaultListWrapper = new LambdaQueryWrapper<>();
            defaultListWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                              .isNull(PluginConfigEntity::getBotId);
            List<PluginConfigEntity> defaultConfigs = this.list(defaultListWrapper);
            
            for (PluginConfigEntity defaultConfig : defaultConfigs) {
                PluginConfigEntity newConfig = new PluginConfigEntity();
                newConfig.setPluginId(pluginId);
                newConfig.setBotId(botId);
                newConfig.setConfigKey(defaultConfig.getConfigKey());
                newConfig.setConfigValue(defaultConfig.getConfigValue());
                newConfig.setConfigType(defaultConfig.getConfigType());
                newConfig.setMetadata(defaultConfig.getMetadata());
                newConfig.setDescription(defaultConfig.getDescription());
                newConfig.setExplain(defaultConfig.getExplain());
                newConfig.setEditable(defaultConfig.getEditable());
                this.save(newConfig);
            }
            
            log.info("懒加载复制所有默认配置到Bot专属配置: pluginId={}, botId={}, count={}", pluginId, botId, defaultConfigs.size());
            
            LambdaQueryWrapper<PluginConfigEntity> newBotWrapper = new LambdaQueryWrapper<>();
            newBotWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                        .eq(PluginConfigEntity::getConfigKey, configKey)
                        .eq(PluginConfigEntity::getBotId, botId);
            botConfig = this.getOne(newBotWrapper);
        } else {
            LambdaQueryWrapper<PluginConfigEntity> defaultWrapper = new LambdaQueryWrapper<>();
            defaultWrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                          .eq(PluginConfigEntity::getConfigKey, configKey)
                          .isNull(PluginConfigEntity::getBotId);
            PluginConfigEntity defaultConfig = this.getOne(defaultWrapper);
            
            if (defaultConfig == null) {
                return false;
            }
            
            ConfigType configType = ConfigType.fromCode(defaultConfig.getConfigType());
            if (!configTypeHandler.validate(configType, configValue)) {
                log.warn("配置值验证失败: type={}, value={}", configType, configValue);
            }
            
            PluginConfigEntity newConfig = new PluginConfigEntity();
            newConfig.setPluginId(pluginId);
            newConfig.setBotId(botId);
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigType(defaultConfig.getConfigType());
            newConfig.setMetadata(defaultConfig.getMetadata());
            newConfig.setDescription(defaultConfig.getDescription());
            newConfig.setExplain(defaultConfig.getExplain());
            newConfig.setEditable(defaultConfig.getEditable());
            this.save(newConfig);
            
            Plugins plugin = pluginsService.getById(pluginId);
            String pluginName = plugin != null ? plugin.getPluginName() : "unknown";

            mangoEventPublisher.publish(new PluginConfigChangedEvent(
                    newConfig.getId(),
                    pluginId,
                    pluginName,
                    configKey,
                    newConfig.getConfigType(),
                    defaultConfig.getConfigValue(),
                    configValue
            ));
            
            log.info("懒加载创建单个Bot专属插件配置: pluginId={}, botId={}, key={}", pluginId, botId, configKey);
            return true;
        }
        
        if (botConfig == null) {
            return false;
        }
        
        ConfigType configType = ConfigType.fromCode(botConfig.getConfigType());
        if (!configTypeHandler.validate(configType, configValue)) {
            log.warn("配置值验证失败: type={}, value={}", configType, configValue);
        }
        
        String oldValue = botConfig.getConfigValue();
        botConfig.setConfigValue(configValue);
        botConfig.setUpdatedAt(System.currentTimeMillis());
        boolean result = this.updateById(botConfig);

        if (result) {
            Plugins plugin = pluginsService.getById(pluginId);
            String pluginName = plugin != null ? plugin.getPluginName() : "unknown";

            mangoEventPublisher.publish(new PluginConfigChangedEvent(
                    botConfig.getId(),
                    pluginId,
                    pluginName,
                    configKey,
                    botConfig.getConfigType(),
                    oldValue,
                    configValue
            ));
            log.info("更新插件配置值成功: pluginId={}, botId={}, key={}", pluginId, botId, configKey);
        }
        return result;
    }

    @Override
    public void deleteByPluginId(Long pluginId) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId);
        List<PluginConfigEntity> configs = this.list(wrapper);
        this.remove(wrapper);
        log.info("删除插件配置: pluginId={}, count={}", pluginId, configs.size());
    }

    private PluginConfigVO convertToVO(PluginConfigEntity config) {
        PluginConfigVO vo = new PluginConfigVO();
        vo.setId(config.getId());
        vo.setPluginId(config.getPluginId());
        vo.setBotId(config.getBotId());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
        vo.setMetadata(parseMetadata(config.getMetadata()));
        vo.setDescription(config.getDescription());
        vo.setExplain(config.getExplain());
        vo.setEditable(config.getEditable() == 1);
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());

        Plugins plugin = pluginsService.getById(config.getPluginId());
        if (plugin != null) {
            vo.setPluginName(plugin.getPluginName());
        }
        return vo;
    }

    private ConfigMetadata parseMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(metadata, ConfigMetadata.class);
        } catch (JsonProcessingException e) {
            log.warn("解析插件配置元数据失败: {}", metadata, e);
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
            log.warn("序列化插件配置元数据失败", e);
            return null;
        }
    }
}
