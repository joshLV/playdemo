package unit;

import com.uhuila.common.util.PathUtil;
import factory.FactoryBoy;
import models.sales.Brand;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.modules.paginate.ModelPaginator;
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
    Brand brand;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();

        brand = FactoryBoy.create(Brand.class);

    }

    @Test
    public void testFindTopByBrand() {
        int limit = 4;

        List<Brand> brands = Brand.findTop(limit, brand.id);

        assertEquals(1, brands.size());


    }

    @Test
    public void testFindByOrder() {
        Supplier supplier = Supplier.findById(brand.supplier.id);
        List<Brand> brands = Brand.findByOrder(supplier);

        assertEquals(1, brands.size());

    }

    @Test
    public void testGetBrandPage() {
        ModelPaginator brandPage = Brand.getBrandPage(1, 15, brand.supplier.id,null);
        assertEquals(1, brandPage.size());

        Brand firstBrand = (Brand) brandPage.get(0);
        assertEquals("来一份", firstBrand.name);
    }

    @Test
    public void testGetOriginalLogo() {
        String imageServer = Play.configuration.getProperty
                ("image.server", "img0.dev.uhcdn.com");
        String imageURL = PathUtil.getImageUrl(imageServer, "/0/0/0/logo.jpg", "nw");

        assertEquals(imageURL, brand.getOriginalLogo());
    }

    @Test
    // TODO need to be improved
    public void testUpdateNullBrand() {
        long emptyId = 123L;
        Brand.update(emptyId, brand);
        Brand updatedBrand = Brand.findById(emptyId);
        assertNotSame(updatedBrand, brand);
    }
}
