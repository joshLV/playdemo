package unit;

import java.util.List;

import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.Fixtures;
import play.test.UnitTest;
import com.uhuila.common.constants.DeletedStatus;

/**
 * 供应商商户单元测试.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 4:52 PM
 */

/**
 * Juno
 * 7/23/12
 * 添加测试
 */

public class SupplierUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Supplier.class);
        Fixtures.loadModels("fixture/supplier_unit.yml");
    }

    @Test
    public void testFindUnDeleted() {
        List<Supplier> suppliers = Supplier.findUnDeleted();
        assertEquals(2, suppliers.size());
        assertEquals("localhost", suppliers.get(0).domainName);
    }

    @Test
    public void testGetSmallLogo(){
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = Supplier.findById(id);
        String imageSever = "http://"+Play.configuration.getProperty
                ("image.server", "img0.uhcdn.com");
        System.out.println("image server is "+imageSever);
        String imageURL = PathUtil.getImageUrl(imageSever, "/0/0/0/logo.jpg", "172x132");
        System.out.println("image URL is "+imageURL);
        assertEquals(imageURL, supplier.getSmallLogo());
    }

    @Test
    public void testFreeze() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier.freeze(id);
        Supplier supplier = Supplier.findById(id);
        assertEquals(SupplierStatus.FREEZE, supplier.status);
    }

    @Test
    public void testUnfreeze() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier.unfreeze(id);
        Supplier supplier = Supplier.findById(id);
        assertEquals(SupplierStatus.NORMAL, supplier.status);
    }

    @Test
    // Check updateAt() 方法不可行，因为update的时候没有改变, 比较supplier.Date()没有意义
    public void testUpdateWithNull(){
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = Supplier.findById(id);
        // generate an empty id
        Long emptyId = 123456789L;
        Supplier.update(emptyId,supplier);
        Supplier updatedSupplier = Supplier.findById(emptyId);
        // Compare that the emptyId was not updated
        assertNotSame(updatedSupplier,supplier);
    }

    @Test
    public void testUpdate() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = new Supplier(id);
        supplier.id = id;
        supplier.domainName = "updated.localhost";
        Supplier.update(id, supplier);
        Supplier updatedSupplier = Supplier.findById(id);
        assertEquals("updated.localhost", updatedSupplier.domainName);
    }

    @Test
    public void testDeleteNull(){
        // record old size
        List<Supplier> suppliers = Supplier.findUnDeleted();
        int oldSize = suppliers.size();
        // nothing is deleted
        Long emptyId = 123456789L;
        Supplier.delete(emptyId);
        // record new size
        suppliers = Supplier.findUnDeleted();
        int newSize = suppliers.size();
        // compare
        assertEquals(oldSize, newSize);
    }

    @Test
    public void testDelete() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier.delete(id);
        Supplier supplier = Supplier.findById(id);
        assertEquals(DeletedStatus.DELETED, supplier.deleted);
    }

    @Test
    public void testFindListByFullName(){
        List<Supplier> suppliers = Supplier.findListByFullName("本地");
        assertEquals(1, suppliers.size());
    }

    @Test
    public void testExistDomainName(){
        assertTrue(Supplier.existDomainName("localhost"));
    }
}
