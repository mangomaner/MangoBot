package io.github.mangomaner.mangobot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mango 工具注解 - 标记在类上，用于定义工具的元数据
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MangoTool {
    
    /**
     * 工具名称
     */
    String name();
    
    /**
     * 工具描述
     */
    String description() default "";
    
    /**
     * 工具分类
     */
    String category() default "CUSTOM";
}
