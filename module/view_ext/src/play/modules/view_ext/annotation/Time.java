package play.modules.view_ext.annotation;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 手机号.
 * <p/>
 * User: sujie
 * Date: 4/12/12
 * Time: 5:31 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = TimeCheck.class)
public @interface Time {
    String message() default TimeCheck.mes;
}
