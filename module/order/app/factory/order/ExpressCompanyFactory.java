package factory.order;

import factory.ModelFactory;
import models.order.ExpressCompany;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-15
 * Time: 上午11:49
 */
public class ExpressCompanyFactory extends ModelFactory<ExpressCompany> {
    @Override
    public ExpressCompany define() {

        ExpressCompany expressCompany = new ExpressCompany();
        expressCompany.code = "test";
        expressCompany.name = "test快递";
        return expressCompany;
    }
}
