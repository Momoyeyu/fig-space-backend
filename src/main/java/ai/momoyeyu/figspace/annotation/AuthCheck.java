package ai.momoyeyu.figspace.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)             // 注解面向目标
@Retention(RetentionPolicy.RUNTIME)     // 注解生效时刻
public @interface AuthCheck {

    /**
     * 控制必须有的权限（Role）
     */
    String mustRole() default "";

}
