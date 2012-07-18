package unit;

import java.util.Calendar;
import java.util.List;
import jobs.report.ReportGenerateJob;
import models.order.Order;
import models.order.OrderItems;
import models.report.DetailDailyReport;
import models.report.GoodsDailyReport;
import models.report.ShopDailyReport;
import models.report.TotalDailyReport;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * 报表生成任务测试.
 * <p/>
 * User: sujie
 * Date: 5/22/12
 * Time: 10:19 AM
 */
public class ReportGenerateJobTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(DetailDailyReport.class);
        Fixtures.delete(ShopDailyReport.class);
        Fixtures.delete(GoodsDailyReport.class);
        Fixtures.delete(TotalDailyReport.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Supplier.class);
        Fixtures.loadModels("fixture/suppliers_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/detail_daily_reports.yml");
        Fixtures.loadModels("fixture/shop_daily_reports.yml");
        Fixtures.loadModels("fixture/goods_daily_reports.yml");
        Fixtures.loadModels("fixture/total_daily_reports.yml");
    }

    @Ignore
    @Test
    public void testDoJob() throws InterruptedException {
        assertEquals(1, Supplier.findAll().size());
        //先设置商品的supplierId
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        System.out.println("supplierId:" + supplierId);
        List<Brand> brands = Brand.findAll();
        for (Brand brand : brands) {
            brand.supplier = new Supplier(supplierId);
            brand.save();
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE,cal.get(Calendar.DATE)-1);
        List<Order> orderList = Order.findAll();
        for (Order order : orderList) {
            order.createdAt = cal.getTime();
            order.save();
        }
        List<OrderItems> orderItemsList = OrderItems.findAll();
        for (OrderItems orderItems : orderItemsList) {
            orderItems.createdAt = cal.getTime();
            orderItems.save();
        }
        List<Goods> goodsList = Goods.findAll();
        for (Goods goods : goodsList) {
            goods.supplierId = supplierId;
            goods.save();
        }
        List<Shop> shops = Shop.findAll();
        for (Shop shop : shops) {
            shop.supplierId = supplierId;
            shop.save();
        }
        List<OrderItems> items = OrderItems.findAll();
        assertEquals(3, items.size());
        List<DetailDailyReport> dailyReports = DetailDailyReport.findAll();
        assertEquals(1, dailyReports.size());

        ReportGenerateJob job = new ReportGenerateJob();
        job.doJob();

        dailyReports = DetailDailyReport.findAll();
        assertEquals(5, dailyReports.size());
        List<ShopDailyReport> shopReports = ShopDailyReport.findAll();
        assertEquals(2, shopReports.size());
        List<GoodsDailyReport> goodsReports = GoodsDailyReport.findAll();
        assertEquals(2, goodsReports.size());
        List<TotalDailyReport> totalDailyReports = TotalDailyReport.findAll();
        assertEquals(2, totalDailyReports.size());
    }

}
