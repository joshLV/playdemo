package unit.order;

import factory.FactoryBoy;
import models.order.ExpressCompany;
import models.order.Freight;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * User: wangjia
 * Date: 13-5-15
 * Time: 下午5:11
 */
public class FreightTest extends UnitTest {

    Supplier supplier;
    ExpressCompany expressCompany;


    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        expressCompany = FactoryBoy.create(ExpressCompany.class);
        FactoryBoy.create(Freight.class);
        FactoryBoy.create(Freight.class, "beijing4");
        FactoryBoy.create(Freight.class, "chongqing2");
        FactoryBoy.create(Freight.class, "suzhou3");
        FactoryBoy.create(Freight.class, "nanjing5");
        FactoryBoy.create(Freight.class, "other10");
    }

    @Test
    public void testFindFreight() throws Exception {
        String address = "重庆上海街";
        Freight freight = Freight.findFreightRule(supplier, expressCompany,
                address);
        assertEquals("重庆", freight.province);
        assertEquals(BigDecimal.valueOf(2), freight.price);
        BigDecimal price = Freight.findFreight(supplier,expressCompany,address);
        assertEquals(BigDecimal.valueOf(2),price);
    }

    @Test
    public void testFindShanghai() throws Exception {
        String address = "中国上海南京街北京路";
        Freight freight1 = Freight.findFreightRule(supplier, expressCompany,
                address);
        assertEquals("上海", freight1.province);
        assertEquals(BigDecimal.valueOf(1), freight1.price);
    }



    @Test
    public void testOtherFreight() throws Exception {
        String address = "中国上南街北t京路";
        Freight freight1 = Freight.findFreightRule(supplier, expressCompany,
                address);
        assertEquals(Freight.OTHER_PROVICE, freight1.province);
        assertEquals(BigDecimal.valueOf(10), freight1.price);
        BigDecimal price = Freight.findFreight(supplier,expressCompany,address);
        assertEquals(BigDecimal.valueOf(10),price);
    }

    @Test
    public void testFindShanghaiInitially() throws Exception {
        String address = "上海南京街北京路";
        Freight freight1 = Freight.findFreightRule(supplier, expressCompany,
                address);
        assertEquals("上海", freight1.province);
        assertEquals(BigDecimal.valueOf(1), freight1.price);
        BigDecimal price = Freight.findFreight(supplier,expressCompany,address);
        assertEquals(BigDecimal.valueOf(1),price);
    }
}
