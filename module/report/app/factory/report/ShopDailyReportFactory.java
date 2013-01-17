package factory.report;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.report.ShopDailyReport;
import models.sales.Shop;

import java.math.BigDecimal;

/**
 * User: wangjia
 * Date: 12-8-20
 * Time: 下午5:10
 */
public class ShopDailyReportFactory extends ModelFactory<ShopDailyReport> {
    @Override
    public ShopDailyReport define() {
        ShopDailyReport report = new ShopDailyReport();
        Shop shop = FactoryBoy.create(Shop.class);
        report.shop = shop;
        report.buyCount = (long) 20.4;
        report.orderCount = (long) 5;
        report.originalAmount = BigDecimal.valueOf(100);
        return report;

    }

}
