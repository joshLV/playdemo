package unit;

import models.sales.Brand;
import org.junit.Before;
import org.junit.Test;
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
    public void testFindByCompanyId() {
        long companyId = 1;

        List<Brand> brands = Brand.findByCompanyId(companyId);

        assertEquals(3, brands.size());
    }

}
