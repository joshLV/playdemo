package play.modules.view_ext.annotation;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.data.validation.Validation;

import java.util.regex.Pattern;

/**
 * 手机号格式检查.
 * <p/>
 * User: sujie
 * Date: 4/12/12
 * Time: 5:32 PM
 */
public class TimeCheck extends AbstractAnnotationCheck<Time> {

    final static String mes = "validation.invalid";
    static Pattern timePattern = Pattern.compile("^(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])");

    @Override
    public void configure(Time mobile) {
        setMessage(mobile.message());
    }

    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        value = Validation.willBeValidated(value);
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        return timePattern.matcher(value.toString()).matches();
    }

}
