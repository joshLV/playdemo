package play.modules.view_ext.annotation;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.data.validation.Validation;

import java.util.regex.Pattern;

/**
 * 邮政编码.
 * <p/>
 * User: sujie
 * Date: 4/24/12
 * Time: 3:02 PM
 */
public class PostcodeCheck extends AbstractAnnotationCheck<Postcode> {

    final static String mes = "validation.invalid";
    static Pattern pattern = Pattern.compile("^[1-9][0-9]{5}$");

    @Override
    public void configure(Postcode postcode) {
        setMessage(postcode.message());
    }

    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        value = Validation.willBeValidated(value);
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        return pattern.matcher(value.toString()).matches();
    }

}
