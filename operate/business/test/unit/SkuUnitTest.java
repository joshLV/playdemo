package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.sales.Brand;
import models.sales.Sku;
import models.sales.SkuCondition;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-27
 * Time: 下午5:37
 */
public class SkuUnitTest extends UnitTest {
    Sku sku;
    Supplier supplier;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier supplier) {
                supplier.sequenceCode = "0001";
                supplier.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
                supplier.code = "010001";
            }
        });
        sku = FactoryBoy.create(Sku.class);

    }

    @Test
    public void test_Create() {
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        sku1.supplier = sku.supplier;
        sku1.brand = sku.brand;
        sku1.supplierCategory = sku.supplierCategory;
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);
    }

    @Test
    public void test_Create_CodeIs999() {
        sku.sequenceCode = "999";
        sku.code = "S010001999";
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        sku1.supplier = supplier;
        sku1.brand = FactoryBoy.lastOrCreate(Brand.class);
        sku1.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);
        assertEquals("S0100011000", sku1.code);
    }

    @Test
    public void test_Create_CodeIs99() {
        sku.sequenceCode = "99";
        sku.code = "S01000199";
        assertEquals(1, Sku.count());
        Sku sku1 = new Sku();
        sku1.name = "test";
        sku1.supplier = supplier;
        sku1.brand = sku.brand;
        sku1.supplierCategory = sku.supplierCategory;
        sku1.create();
        assertEquals(2, Sku.count());
        assertEquals("S" + sku1.supplierCategory.code + supplier.sequenceCode + sku1.sequenceCode, sku1.code);
        assertEquals("S010001100", sku1.code);

    }

    @Test
    public void testUpdate() {
        sku.code = "002";
        Sku.update(sku.id, sku);
        sku.refresh();
        assertEquals("002", sku.code);

    }

    @Test
    public void testDelete() {
        assertEquals(DeletedStatus.UN_DELETED, sku.deleted);
        Sku.delete(sku.id);
        assertEquals(DeletedStatus.DELETED, sku.deleted);

        SkuCondition condition = new SkuCondition();
        List<Sku> skuList = Sku.findByCondition(condition, 1, 15);
        assertEquals(0, skuList.size());
    }

    @Test
    public void testIndex() {
        SkuCondition condition = new SkuCondition();
        List<Sku> skuList = Sku.findByCondition(condition, 1, 15);
        assertEquals(1, skuList.size());
    }


}
