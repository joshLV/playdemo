package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.sales.InventoryStock;
import models.sales.Sku;
import models.sales.StockActionType;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;

/**
 * 库存变动单单元测试
 * <p/>
 * User: wangjia
 * Date: 13-3-18
 * Time: 下午2:22
 */
public class InventoryStockUnitTest extends UnitTest {
    InventoryStock stock;
    String date;


    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        stock = FactoryBoy.create(InventoryStock.class, new BuildCallback<InventoryStock>() {
            @Override
            public void build(InventoryStock stock) {
                date = com.uhuila.common.util.DateUtil.dateToString(new Date(), "yyyyMMdd");
                stock.actionType = StockActionType.IN;
                stock.serialNo = stock.actionType.getCode() + date + "01";
            }
        });
    }

    @Test
    public void test_Create() {
        assertEquals(1, InventoryStock.count());
        InventoryStock stock1 = new InventoryStock();
        stock1.actionType = StockActionType.IN;
        stock1.supplier = stock.supplier;
        stock1.create();
        assertEquals(2, InventoryStock.count());
        String date = com.uhuila.common.util.DateUtil.dateToString(new Date(), "yyyyMMdd");
        assertEquals("J" + date + "02", stock1.serialNo);
    }

    @Test
    public void test_Create_CodeIs999() {
        stock.serialNo = stock.actionType.getCode() + date + "99";
        assertEquals(1, InventoryStock.count());
        InventoryStock stock1 = new InventoryStock();
        stock1.actionType = StockActionType.IN;
        stock1.supplier = stock.supplier;
        stock1.create();
        assertEquals(2, InventoryStock.count());
        assertEquals("J" + date + "100", stock1.serialNo);

    }

    @Test
    public void test_Create_CodeIs99() {
        stock.serialNo = stock.actionType.getCode() + date + "999";
        assertEquals(1, InventoryStock.count());
        InventoryStock stock1 = new InventoryStock();
        stock1.actionType = StockActionType.IN;
        stock1.supplier = stock.supplier;
        stock1.create();
        assertEquals(2, InventoryStock.count());
        assertEquals("J" + date + "1000", stock1.serialNo);
    }


}
