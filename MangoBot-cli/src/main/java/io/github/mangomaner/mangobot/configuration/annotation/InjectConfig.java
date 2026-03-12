package io.github.mangomaner.mangobot.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插件配置注入注解
 * 用于在插件类中标记需要注入的配置字段
 * 
 * <p>使用示例：
 * <pre>
 * public class MyPlugin implements Plugin {
 *     &#64;InjectConfig(key = "timeout", defaultValue = "30", description = "超时时间（秒）")
 *     private Integer timeout;
 *     
 *     &#64;InjectConfig(key = "greeting", defaultValue = "你好", description = "问候语")
 *     private String greeting;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectConfig {

    /**
     * 配置键（相对于插件）
     * 实际存储时会自动添加插件前缀
     */
    String key();

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 描述
     */
    String description() default "";

    /**
     * 配置类型
     * 支持：STRING, INTEGER, BOOLEAN, JSON
     */
    String type() default "STRING";
}
