package io.github.mangomaner.mangobot.annotation;

import io.github.mangomaner.mangobot.module.configuration.annotation.ConfigMeta;
import io.github.mangomaner.mangobot.module.configuration.enums.ConfigType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PluginConfigItem {
    
    String key();
    
    String value() default "";
    
    ConfigType type() default ConfigType.STRING;
    
    String description() default "";
    
    String explain() default "";
    
    String category() default "general";
    
    boolean editable() default true;
    
    ConfigMeta metadata() default @ConfigMeta;
}
