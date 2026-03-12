package io.github.mangomaner.mangobot.configuration.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOption implements Serializable {
    
    private String label;
    
    private String value;
    
    private Boolean disabled;
    
    public SelectOption(String label, String value) {
        this.label = label;
        this.value = value;
        this.disabled = false;
    }
}
