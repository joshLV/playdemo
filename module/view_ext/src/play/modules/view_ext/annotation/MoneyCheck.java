package play.modules.view_ext.annotation;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.data.validation.Validation;

import java.util.regex.Pattern;

/**
 * 检查钱的格式.
 * <p/>
 * User: sujie
 * Date: 4/10/12
 * Time: 4:59 PM
 */
public class MoneyCheck extends AbstractAnnotationCheck<Money> {

    final static String mes = "validation.money";
    static Pattern moneyPattern = Pattern.compile("^([0-9]+|[0-9]{1,3}(,[0-9]{3})*)(.[0-9]{1,2})?$");

    @Override
    public void configure(Money money) {
        setMessage(money.message());
    }

    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        value = Validation.willBeValidated(value);
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        return moneyPattern.matcher(value.toString()).matches();
    }

}
