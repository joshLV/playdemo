package util.extension.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展点标注。
 *
 * 用于标注一类扩展点，ExtensionInvoker基于标注类的子类进行扩展。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ExtensionPoint {
    String value();
}
