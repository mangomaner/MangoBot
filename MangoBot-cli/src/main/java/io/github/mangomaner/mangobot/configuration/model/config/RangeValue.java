package io.github.mangomaner.mangobot.configuration.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangeValue<T extends Number> implements Serializable {
    
    private T min;
    
    private T max;
    
    private T step;
    
    public RangeValue(T min, T max) {
        this.min = min;
        this.max = max;
        this.step = null;
    }
}
