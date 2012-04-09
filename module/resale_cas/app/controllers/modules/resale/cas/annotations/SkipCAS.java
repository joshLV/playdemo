package controllers.modules.resale.cas.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果一个方法或一个类标注为SkipCAS，则不需要进行CAS检查.
 * @author <a href="mailto:tangliqun@snda.com">唐力群</a>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SkipCAS {

}