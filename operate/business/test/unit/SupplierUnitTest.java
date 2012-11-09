package unit;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.Fixtures;
import play.test.UnitTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    public void testGetSmallLogo() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = Supplier.findById(id);
        String imageSever = "http://" + Play.configuration.getProperty
                ("image.server", "img0.uhcdn.com");
        String imageURL = PathUtil.getImageUrl(imageSever, "/0/0/0/logo.jpg", "172x132");
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
    public void testUpdateWithNull() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = Supplier.findById(id);
        // generate an empty id
        Long emptyId = 123456789L;
        Supplier.update(emptyId, supplier);
        Supplier updatedSupplier = Supplier.findById(emptyId);
        // Compare that the emptyId was not updated
        assertNotSame(updatedSupplier, supplier);
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
    public void testDeleteNull() {
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
    public void testFindListByFullName() {
        List<Supplier> suppliers = Supplier.findListByFullName("本地");
        assertEquals(1, suppliers.size());
    }

    @Test
    public void testExistDomainName() {
        assertTrue(Supplier.existDomainName("localhost"));
    }

    @Test
    public void testShopHour() {
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = new Date();
        Date beginDate = Supplier.getShopHour(date, "22:00", false);
        Date endDate = Supplier.getShopHour(date, "22:00", true);
        assertEquals((DateUtil.dateToString(date, 0) + " 22:00"), formatDate.format(beginDate));
        assertEquals(DateUtil.dateToString(date, 1) + " 22:00", formatDate.format(endDate));
        beginDate = Supplier.getShopHour(date, "", false);
        endDate = Supplier.getShopHour(date, "", true);
        assertEquals(DateUtil.dateToString(date, 0) + " 23:59", formatDate.format(beginDate));
        assertEquals(DateUtil.dateToString(date, 1) + " 23:59", formatDate.format(endDate));
    }

}
