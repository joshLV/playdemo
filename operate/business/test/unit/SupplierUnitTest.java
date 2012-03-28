package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * 供应商商户单元测试.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 4:52 PM
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
    public void testUpdate() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier supplier = new Supplier();
        supplier.id = id;
        supplier.domainName = "updated.localhost";
        Supplier.update(id, supplier);
        Supplier updatedSupplier = Supplier.findById(id);
        assertEquals("updated.localhost", updatedSupplier.domainName);
    }

    @Test
    public void testDelete() {
        Long id = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Supplier.delete(id);
        Supplier supplier = Supplier.findById(id);
        assertEquals(DeletedStatus.DELETED, supplier.deleted);
    }
}
