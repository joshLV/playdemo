package unit;

import models.sales.Brand;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * 品牌的单元测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 4:14 PM
 */
public class BrandUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Brand.class);
        Fixtures.delete(Supplier.class);

        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/brands.yml");
    }

    @Test
    public void testFindTopByBrand() {
        int limit = 4;
        long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_6");

        List<Brand> brands = Brand.findTop(limit, brandId);

        assertEquals(4, brands.size());
    }

    @Test
    public void testFindByOrder() {

        List<Brand> brands = Brand.findByOrder();

        assertEquals(10, brands.size());
    }

    @Test
    public void testGetBrandPage() {
//        long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        ModelPaginator brandPage = Brand.getBrandPage(1, 15, 1L);
        assertEquals(3, brandPage.size());
        Brand firstBrand = (Brand) brandPage.get(0);
        assertEquals("来一份", firstBrand.name);
    }

}
