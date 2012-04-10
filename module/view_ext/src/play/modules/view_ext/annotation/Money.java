package play.modules.view_ext.annotation;

/**
 * 钱的标注.
 * <p/>
 * User: sujie
 * Date: 4/10/12
 * Time: 4:58 PM
 */
import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field must be a valid email.
 * Message key: validation.email
 * $1: field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = MoneyCheck.class)

public @interface Money {

    String message() default MoneyCheck.mes;
}

