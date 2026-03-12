package io.github.mangomaner.mangobot.configuration.annotation;

import io.github.mangomaner.mangobot.configuration.enums.ConfigType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectConfig {

    String key();

    String defaultValue() default "";

    String description() default "";

    String explain() default "";

    ConfigType type() default ConfigType.STRING;

    String category() default "general";

    boolean editable() default true;

    ConfigMeta metadata() default @ConfigMeta;
}
