package factory.sales;
import models.sales.Brand;
import models.supplier.Supplier;
import factory.FactoryBoy;
import factory.ModelFactory;

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
        brand.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        brand.name = "来一份";
        brand.displayOrder=100;
        return brand;
    }

}
