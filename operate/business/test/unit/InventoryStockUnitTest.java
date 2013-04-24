package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.Sku;
import models.sales.StockActionType;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

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
    InventoryStockItem stockItem1;
    InventoryStockItem stockItem2;
    Sku sku;
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
//        stock.inventoryStockItemList = new LinkedList<>();
//        stock.inventoryStockItemList.add(stockItem1);
//        stock.inventoryStockItemList.add(stockItem2);
//        stock.save();
        sku = FactoryBoy.create(Sku.class);
        stockItem1 = FactoryBoy.create(InventoryStockItem.class, new BuildCallback<InventoryStockItem>() {
            @Override
            public void build(InventoryStockItem i) {
                i.changeCount = 81l;
                i.remainCount = 81l;
                i.createdAt = new Date();
            }
        });
        stockItem2 = FactoryBoy.create(InventoryStockItem.class, new BuildCallback<InventoryStockItem>() {
            @Override
            public void build(InventoryStockItem i) {
                i.changeCount = 70l;
                i.remainCount = 70l;
                i.createdAt = DateHelper.afterDays(new Date(), 5);
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

    @Test
    public void test_Update_Inventory_Stock_Remain_Count() {
        InventoryStock.updateInventoryStockRemainCount(sku, 90l);
        stockItem1.refresh();
        assertEquals(Long.valueOf(61), stockItem2.remainCount);
    }


}
