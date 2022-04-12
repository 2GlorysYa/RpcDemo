package Server.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)   // 表示注解的作用目标为接口、类、枚举类型
@Retention(RetentionPolicy.RUNTIME) // 表示在运行时可以动态获取注解信息
public @interface Service {

    public String name() default "";
}
