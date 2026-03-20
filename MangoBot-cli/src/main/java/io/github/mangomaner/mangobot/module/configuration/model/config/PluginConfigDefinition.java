package io.github.mangomaner.mangobot.module.configuration.model.config;

import io.github.mangomaner.mangobot.module.configuration.enums.ConfigType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfigDefinition implements Serializable {
    
    private String key;
    
    private String value;
    
    private ConfigType type;
    
    private String description;
    
    private String explain;
    
    private String category;
    
    private Boolean editable;
    
    private ConfigMetadata metadata;
    
    public static PluginConfigDefinition of(String key, String value, ConfigType type, 
                                            String description, String explain, 
                                            String category, Boolean editable, 
                                            ConfigMetadata metadata) {
        return PluginConfigDefinition.builder()
                .key(key)
                .value(value)
                .type(type)
                .description(description)
                .explain(explain)
                .category(category)
                .editable(editable)
                .metadata(metadata)
                .build();
    }
}
