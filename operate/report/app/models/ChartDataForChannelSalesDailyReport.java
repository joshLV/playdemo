package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: wangjia
 * Date: 13-4-12
 * Time: 下午11:10
 */
public class ChartDataForChannelSalesDailyReport {
    public String date;
    public BigDecimal DDNetSalesAmount;
    public BigDecimal YHDNetSalesAmount;
    public BigDecimal JDNetSalesAmount;
    public BigDecimal WBNetSalesAmount;
    public BigDecimal TBNetSalesAmount;
    public BigDecimal YBQNetSalesAmount;

    public static List<ChartDataForChannelSalesDailyReport> mapTrendsCharts(
            List<ChartDataForChannelSalesDailyReport> totalSales, List<String> dateList) {
        Map<String, ChartDataForChannelSalesDailyReport> map = new HashMap<>();
        for (ChartDataForChannelSalesDailyReport report : totalSales) {
            map.put(report.date, report);
        }

        List<ChartDataForChannelSalesDailyReport> list = new ArrayList<>();
        for (String date : dateList) {
            ChartDataForChannelSalesDailyReport report = map.get(date);
            if (report == null) {
                report = new ChartDataForChannelSalesDailyReport();
                report.date = date;
            }
            list.add(report);
        }
        return list;
    }

}
