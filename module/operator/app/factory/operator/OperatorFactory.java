package factory.operator;

import factory.ModelFactory;
import models.operator.Operator;

/**
 * 运营商.
 * <p/>
 * User: wangjia
 * Date: 13-5-13
 * Time: 上午10:32
 */
public class OperatorFactory extends ModelFactory<Operator> {
    public final static String DEFAULT_OPERATOR_CODE = "SHIHUI";

    @Override
    public Operator define() {
        Operator operator = new Operator();
        operator.name = "上海视惠运营平台";
        operator.code = DEFAULT_OPERATOR_CODE;
        return operator;
    }
}
