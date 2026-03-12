package io.github.mangomaner.mangobot.configuration.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValueItem implements Serializable {
    
    private String key;
    
    private String value;
    
    private String description;
    
    public KeyValueItem(String key, String value) {
        this.key = key;
        this.value = value;
        this.description = null;
    }
}
