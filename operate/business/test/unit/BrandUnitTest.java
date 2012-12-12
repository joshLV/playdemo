package unit;

import java.util.List;

<<<<<<< Updated upstream
=======
import com.uhuila.common.util.PathUtil;
import controllers.OperateRbac;
>>>>>>> Stashed changes
import models.sales.Brand;
import models.supplier.Supplier;

import operate.rbac.ContextedPermission;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

import com.uhuila.common.util.PathUtil;

import factory.FactoryBoy;

/**
 * 品牌的单元测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 4:14 PM
 */
public class BrandUnitTest extends UnitTest {
<<<<<<< Updated upstream

    Brand brand;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();

        brand = FactoryBoy.create(Brand.class);
=======
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Brand.class);
        Fixtures.delete(Supplier.class);

        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/brands.yml");
>>>>>>> Stashed changes
    }

    @Test
    public void testFindTopByBrand() {
        int limit = 4;
<<<<<<< Updated upstream

        List<Brand> brands = Brand.findTop(limit, brand.id);

        assertEquals(1, brands.size());

=======
        long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_6");

        List<Brand> brands = Brand.findTop(limit, brandId);

        assertEquals(4, brands.size());
>>>>>>> Stashed changes
    }

    @Test
    public void testFindByOrder() {
<<<<<<< Updated upstream
        Supplier supplier = Supplier.findById(brand.supplier.id);
        List<Brand> brands = Brand.findByOrder(supplier);

        assertEquals(1, brands.size());
=======
        long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = Supplier.findById(supplierId);
        List<Brand> brands = Brand.findByOrder(supplier, null, true);

        assertEquals(3, brands.size());
>>>>>>> Stashed changes
    }

    @Test
    public void testGetBrandPage() {
<<<<<<< Updated upstream
        ModelPaginator brandPage = Brand.getBrandPage(1, 15, brand.supplier.id);
        assertEquals(1, brandPage.size());
=======
        long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        ModelPaginator brandPage = Brand.getBrandPage(1, 15, supplierId);
        assertEquals(3, brandPage.size());
>>>>>>> Stashed changes
        Brand firstBrand = (Brand) brandPage.get(0);
        assertEquals("来一份", firstBrand.name);
    }

    @Test
    public void testGetOriginalLogo() {
<<<<<<< Updated upstream
        String imageServer = Play.configuration.getProperty
                ("image.server", "img0.dev.uhcdn.com");
        String imageURL = PathUtil.getImageUrl(imageServer, "/0/0/0/logo.jpg", "nw");
=======
        long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Brand brand = Brand.findById(brandId);
        String imageServer = Play.configuration.getProperty
                ("image.server", "img0.dev.uhcdn.com");
        System.out.println("Image Server:  " + imageServer);
        String imageURL = PathUtil.getImageUrl(imageServer, "/0/0/0/logo.jpg", "nw");
        System.out.println("Image URL: " + imageURL);
>>>>>>> Stashed changes
        assertEquals(imageURL, brand.getOriginalLogo());
    }

    @Test
    // TODO need to be improved
    public void testUpdateNullBrand() {
<<<<<<< Updated upstream
=======
        long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Brand brand = Brand.findById(brandId);
>>>>>>> Stashed changes
        long emptyId = 123L;
        Brand.update(emptyId, brand);
        Brand updatedBrand = Brand.findById(emptyId);
        assertNotSame(updatedBrand, brand);
    }
}
