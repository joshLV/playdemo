package unit;

import java.util.List;

import jobs.report.ReportGenerateJob;
import models.order.OrderItems;
import models.report.DetailDailyReport;
import models.report.GoodsDailyReport;
import models.report.ShopDailyReport;
import models.report.TotalDailyReport;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;

/**
 * 报表生成任务测试.
 * <p/>
 * User: sujie
 * Date: 5/22/12
 * Time: 10:19 AM
 */
public class ReportGenerateJobTest extends UnitTest {
    Supplier supplier;
    
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        FactoryBoy.create(Brand.class);
        FactoryBoy.create(Shop.class);
        FactoryBoy.create(Goods.class);
        FactoryBoy.create(OrderItems.class);

    }

    @Ignore
    @Test
    public void testDoJob() throws InterruptedException {
        assertEquals(1, Supplier.findAll().size());

        List<OrderItems> items = OrderItems.findAll();
        assertEquals(1, items.size());
        List<DetailDailyReport> dailyReports = DetailDailyReport.findAll();
        assertEquals(0, dailyReports.size());

        ReportGenerateJob job = new ReportGenerateJob();
        job.doJob();

        dailyReports = DetailDailyReport.findAll();
        assertEquals(1, dailyReports.size());
        List<ShopDailyReport> shopReports = ShopDailyReport.findAll();
        assertEquals(1, shopReports.size());
        List<GoodsDailyReport> goodsReports = GoodsDailyReport.findAll();
        assertEquals(1, goodsReports.size());
        List<TotalDailyReport> totalDailyReports = TotalDailyReport.findAll();
        assertEquals(1, totalDailyReports.size());
    }

}
