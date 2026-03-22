package io.github.mangomaner.mangobot.module.configuration.core;

import io.github.mangomaner.mangobot.annotation.ConfigMeta;
import io.github.mangomaner.mangobot.annotation.InjectConfig;
import io.github.mangomaner.mangobot.module.configuration.model.config.ConfigMetadata;
import io.github.mangomaner.mangobot.module.configuration.model.config.PluginConfigDefinition;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PluginConfigRegistry {

    private final PluginConfigService pluginConfigService;

    public PluginConfigRegistry(PluginConfigService pluginConfigService) {
        this.pluginConfigService = pluginConfigService;
    }

    public void scanAndRegister(Class<?> pluginClass, Long pluginId) {
        List<PluginConfigDefinition> definitions = scanConfigDefinitions(pluginClass);
        
        pluginConfigService.deleteByPluginId(pluginId);
        
        for (PluginConfigDefinition def : definitions) {
            pluginConfigService.registerDefinition(pluginId, def);
            log.debug("注册插件配置: {} = {}", def.getKey(), def.getValue());
        }
        
        log.info("插件配置注册完成，共 {} 项", definitions.size());
    }

    public List<PluginConfigDefinition> scanConfigDefinitions(Class<?> pluginClass) {
        List<PluginConfigDefinition> definitions = new ArrayList<>();

        for (Field field : pluginClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectConfig.class)) {
                InjectConfig inject = field.getAnnotation(InjectConfig.class);
                definitions.add(createDefinitionFromInject(inject));
            }
        }
        
        return definitions;
    }

    private PluginConfigDefinition createDefinitionFromInject(InjectConfig inject) {
        ConfigMetadata metadata = createMetadataFromAnnotation(inject.metadata());
        
        return PluginConfigDefinition.builder()
                .key(inject.key())
                .value(inject.defaultValue())
                .type(inject.type())
                .description(inject.description())
                .explain(inject.explain())
                .category(inject.category())
                .editable(inject.editable())
                .metadata(metadata)
                .build();
    }

    private ConfigMetadata createMetadataFromAnnotation(ConfigMeta meta) {
        ConfigMetadata metadata = new ConfigMetadata();
        
        if (!meta.placeholder().isEmpty()) {
            metadata.setPlaceholder(meta.placeholder());
        }
        if (!meta.keyPlaceholder().isEmpty()) {
            metadata.setKeyPlaceholder(meta.keyPlaceholder());
        }
        if (!meta.valuePlaceholder().isEmpty()) {
            metadata.setValuePlaceholder(meta.valuePlaceholder());
        }
        if (!meta.format().isEmpty()) {
            metadata.setFormat(meta.format());
        }
        if (!meta.itemType().isEmpty()) {
            metadata.setItemType(meta.itemType());
        }
        if (!meta.listType().isEmpty()) {
            metadata.setListType(meta.listType());
        }
        if (!meta.pattern().isEmpty()) {
            metadata.setPattern(meta.pattern());
        }
        
        if (meta.min() != Integer.MAX_VALUE) {
            metadata.setMin(meta.min());
        }
        if (meta.max() != Integer.MAX_VALUE) {
            metadata.setMax(meta.max());
        }
        if (meta.minDouble() != Double.MIN_VALUE) {
            metadata.setMinDouble(meta.minDouble());
        }
        if (meta.maxDouble() != Double.MAX_VALUE) {
            metadata.setMaxDouble(meta.maxDouble());
        }
        if (meta.step() != 1.0) {
            metadata.setStep(meta.step());
        }
        if (meta.rows() > 0) {
            metadata.setRows(meta.rows());
        }
        if (meta.maxLength() > 0) {
            metadata.setMaxLength(meta.maxLength());
        }
        
        metadata.setClearable(meta.clearable());
        metadata.setFilterable(meta.filterable());
        metadata.setShowPassword(meta.showPassword());
        
        return metadata;
    }
}
