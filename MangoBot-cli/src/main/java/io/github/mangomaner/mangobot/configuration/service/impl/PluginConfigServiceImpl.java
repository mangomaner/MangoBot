package io.github.mangomaner.mangobot.configuration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.configuration.event.PluginConfigChangedEvent;
import io.github.mangomaner.mangobot.mapper.configuration.PluginConfigMapper;
import io.github.mangomaner.mangobot.configuration.model.domain.PluginConfigEntity;
import io.github.mangomaner.mangobot.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.PluginConfigVO;
import io.github.mangomaner.mangobot.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.manager.event.MangoEventPublisher;
import io.github.mangomaner.mangobot.model.domain.Plugins;
import io.github.mangomaner.mangobot.service.PluginsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件配置服务实现
 */
@Service
@Slf4j
public class PluginConfigServiceImpl extends ServiceImpl<PluginConfigMapper, PluginConfigEntity>
        implements PluginConfigService {

    @Resource
    private MangoEventPublisher mangoEventPublisher;

    @Resource
    private PluginsService pluginsService;

    @Override
    public List<PluginConfigVO> getAllConfigs() {
        return this.list().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginConfigVO> getConfigsByPluginId(Long pluginId) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public PluginConfigVO getConfig(Long pluginId, String configKey) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey);
        PluginConfigEntity config = this.getOne(wrapper);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    public String getConfigValue(Long pluginId, String configKey) {
        return getConfigValue(pluginId, configKey, null);
    }

    @Override
    public String getConfigValue(Long pluginId, String configKey, String defaultValue) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey);
        PluginConfigEntity config = this.getOne(wrapper);
        if (config == null) {
            return defaultValue;
        }
        return config.getConfigValue() != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public void registerConfig(Long pluginId, String configKey, String configValue,
                                String configType, String description, String explain) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey);
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
            config.setConfigType(configType != null ? configType : "STRING");
            config.setDescription(description);
            config.setExplain(explain);
            config.setEditable(1);
            this.save(config);
            log.debug("注册插件配置: pluginId={}, key={}", pluginId, configKey);
        }
    }

    @Override
    public PluginConfigVO updateConfig(UpdatePluginConfigRequest request) {
        PluginConfigEntity config = this.getById(request.getId());
        if (config == null) {
            return null;
        }

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

        log.info("更新插件配置成功: pluginId={}, key={}", config.getPluginId(), config.getConfigKey());
        return convertToVO(config);
    }

    @Override
    public boolean updateConfigValue(Long pluginId, String configKey, String configValue) {
        LambdaQueryWrapper<PluginConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PluginConfigEntity::getPluginId, pluginId)
                .eq(PluginConfigEntity::getConfigKey, configKey);
        PluginConfigEntity config = this.getOne(wrapper);
        if (config == null) {
            return false;
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
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
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
}
