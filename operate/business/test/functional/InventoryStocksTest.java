package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.Sku;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理的测试用例
 * <p/>
 * User: wangjia
 * Date: 13-3-12
 * Time: 上午10:54
 */
public class InventoryStocksTest extends FunctionalTest {
    Supplier supplier;
    Sku sku;
    InventoryStock stock;
    InventoryStockItem stockItem;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class, "seewi");
        sku = FactoryBoy.create(Sku.class);
        stock = FactoryBoy.create(InventoryStock.class);
        stockItem = FactoryBoy.create(InventoryStockItem.class);
        OperateUser user = FactoryBoy.create(OperateUser.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        Supplier.clearShihuiSupplier();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("InventoryStocks.index").url);
        assertIsOk(response);
        JPAExtPaginator<InventoryStockItem> stockItemList = (JPAExtPaginator) renderArgs("stockItemList");
        assertEquals(1, stockItemList.size());
        assertEquals(stockItem.changeCount, stockItemList.get(0).changeCount);
    }

    @Test
    public void testStockIn() {
        List<Sku> skuss = Sku.findAll();
        Http.Response response = GET(Router.reverse("InventoryStocks.stockIn").url);
        assertIsOk(response);
        List<Sku> skuList = (List) renderArgs("skuList");
        assertEquals(1, skuList.size());
    }

    @Test
    public void testStockOut() {
        Http.Response response = GET(Router.reverse("InventoryStocks.stockOut").url);
        assertIsOk(response);
        List<Sku> skuList = (List) renderArgs("skuList");
        assertEquals(1, skuList.size());
    }

    @Test
    public void testCreateStockIn() {
        long stockItemCount = InventoryStockItem.count();
        Map<String, String> params = new HashMap<>();
        params.put("stockItem.sku.name", sku.name);
        params.put("stockItem.sku.id", sku.id.toString());
        params.put("stockItem.stock.id", stockItem.stock.id.toString());
        params.put("stockItem.changeCount", "10");
        params.put("stockItem.price", "10.0");
        Http.Response response = POST("/stock-in", params);
        assertStatus(302, response);
        assertEquals(stockItemCount + 1, InventoryStockItem.count());
    }

    @Test
    public void testCreateStockOut() {
        long stockItemCount = InventoryStockItem.count();
        assertEquals(10l, sku.getRemainCount());
        Map<String, String> params = new HashMap<>();
        params.put("stockItem.sku.name", sku.name);
        params.put("stockItem.sku.id", sku.id.toString());
        params.put("stockItem.stock.id", stockItem.stock.id.toString());
        params.put("stockItem.changeCount", "1");
        params.put("stockItem.price", "10.0");
        Http.Response response = POST("/stock-out", params);
        assertStatus(302, response);
        assertEquals(stockItemCount + 1, InventoryStockItem.count());
        assertEquals(9l, sku.getRemainCount());
    }

    @Test
    public void testStockInOutCalculation() {
        Sku currentSku = FactoryBoy.create(Sku.class);
        List<InventoryStockItem> stockItemList = FactoryBoy.batchCreate(6, InventoryStockItem.class,
                new SequenceCallback<InventoryStockItem>() {
                    public void sequence(InventoryStockItem target, int seq) {
                    }
                });
        long stockItemCount = InventoryStockItem.count();
        assertEquals(60l, currentSku.getRemainCount());
        Map<String, String> params = new HashMap<>();
        params.put("stockItem.sku.name", currentSku.name);
        params.put("stockItem.sku.id", currentSku.id.toString());
        params.put("stockItem.stock.id", stockItem.stock.id.toString());
        params.put("stockItem.changeCount", "25");
        params.put("stockItem.price", "10.0");
        Http.Response response = POST("/stock-out", params);
        assertStatus(302, response);
        assertEquals(stockItemCount + 1, InventoryStockItem.count());
        assertEquals(35l, currentSku.getRemainCount());

    }


}
