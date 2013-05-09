package models.jobs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 下午2:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JobDefine {
    String title() default "";
    int retainHistoryMinutes() default 30000;
    String description() default "";
}
