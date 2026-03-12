package io.github.mangomaner.mangobot.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface ConfigMeta {
    
    String placeholder() default "";
    
    String keyPlaceholder() default "";
    
    String valuePlaceholder() default "";
    
    String format() default "";
    
    String itemType() default "";
    
    String listType() default "";
    
    String pattern() default "";
    
    int min() default Integer.MAX_VALUE;
    
    int max() default Integer.MAX_VALUE;
    
    double minDouble() default Double.MIN_VALUE;
    
    double maxDouble() default Double.MAX_VALUE;
    
    double step() default 1.0;
    
    boolean clearable() default false;
    
    boolean filterable() default false;
    
    boolean showPassword() default true;
    
    int rows() default 0;
    
    int maxLength() default 0;
}
