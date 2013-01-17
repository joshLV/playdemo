package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
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
 * User: wangjia
 * Date: 12-8-23
 * Time: 下午12:03
 */
public class CategoryFactory extends ModelFactory<Category> {
    @Override
    public Category define() {
        Category category = new Category();

        category.name = "饮食";
        category.displayOrder = 100;
        category.deleted = DeletedStatus.UN_DELETED;
        return category;
    }
}
