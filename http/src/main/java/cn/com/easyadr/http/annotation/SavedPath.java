package cn.com.easyadr.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({PARAMETER})
@Retention(RUNTIME)
public @interface SavedPath {
    public static final int REPLACE = 0;
    public static final int CONTINUOUS = 1;

    int value() default 0;
}
