package cn.com.easyadr.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//http body
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Body {
    String value() default "";
}
