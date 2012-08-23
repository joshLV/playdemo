package factory.sales;
import factory.FactoryBoy;
import models.sales.Brand;
import models.sales.Category;
import factory.ModelFactory;
import models.sales.Area;
import models.sales.Goods;
import models.sales.SecKillGoods;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-23
 * Time: 下午1:52
 * To change this template use File | Settings | File Templates.
 */
public class BrandFactory  extends ModelFactory<Brand> {
    @Override
    public Brand define() {

        Brand brand = new Brand();
        brand.name = "来一份";
        brand.displayOrder=100;
        return brand;
    }

}
