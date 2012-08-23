package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Area;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-23
 * Time: 下午12:03
 * To change this template use File | Settings | File Templates.
 */
public class CategoryFactory extends ModelFactory<Category> {
    @Override
    public Category define() {
        Category category = new Category();

        category.name="饮食";
        category.displayOrder=100;
        return category;


    }
}
