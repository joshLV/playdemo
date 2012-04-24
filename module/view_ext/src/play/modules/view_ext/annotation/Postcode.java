package play.modules.view_ext.annotation;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 邮编.
 * <p/>
 * User: sujie
 * Date: 4/24/12
 * Time: 3:01 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = PostcodeCheck.class)
public @interface Postcode {
    String message() default PostcodeCheck.mes;
}
