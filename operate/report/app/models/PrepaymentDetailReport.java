package models;

import com.uhuila.common.util.DateUtil;
import models.order.ECoupon;
import models.order.OrderItems;
import models.order.Prepayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预付款采购销售详细信息报表.
 * <p/>
 * User: sujie
 * Date: 11/30/12
 * Time: 10:41 AM
 */
public class PrepaymentDetailReport {
    /**
     * 日期
     */
    public Date date;
    /**
     * 预期消费余额
     */
    public BigDecimal expectedConsumedBalance = null;
    /**
     * 预期销售余额
     */
    public BigDecimal expectedSoldBalance = null;
    /**
     * 消费单日金额.
     */
    public BigDecimal consumed = BigDecimal.ZERO;
    /**
     * 消费进度
     */
    public BigDecimal consumedBalance = BigDecimal.ZERO;
    /**
     * 销售单日金额
     */
    public BigDecimal sold = BigDecimal.ZERO;
    /**
     * 销售进度
     */
    public BigDecimal soldBalance = BigDecimal.ZERO;
    /**
     * 可用预付款
     */
    public BigDecimal availableBalance = BigDecimal.ZERO;

    private static BigDecimal lastSoldBalance = BigDecimal.ZERO;
    private static BigDecimal lastConsumedBalance = BigDecimal.ZERO;

    public PrepaymentDetailReport() {
    }

    public PrepaymentDetailReport(Date date, BigDecimal expectedConsumedBalance, BigDecimal expectedSoldBalance,
                                  BigDecimal consumed, BigDecimal consumedBalance, BigDecimal sold, BigDecimal soldBalance) {
        this.date = date;
        this.expectedConsumedBalance = expectedConsumedBalance;
        this.expectedSoldBalance = expectedSoldBalance;
        this.consumed = consumed;
        this.consumedBalance = consumedBalance;
        this.sold = sold;
        this.soldBalance = soldBalance;
    }

    public static Map<String, PrepaymentDetailReport> find(Prepayment prepayment, List<String> sortedDateList) {
        Map<String, PrepaymentDetailReport> reportMap = new HashMap<>();

        BigDecimal consumedAmount = BigDecimal.ZERO;
        BigDecimal soldAmount = BigDecimal.ZERO;
        final Date firstDay = DateUtil.stringToDate(sortedDateList.get(0), "yyyy-M-dd");
        final Date lastDay = DateUtil.stringToDate(sortedDateList.get(sortedDateList.size() - 1), "yyyy-M-dd");
        final long totalDayCount = (lastDay.getTime() - firstDay.getTime()) / (3600 * 1000 * 24);
        final long expectedDayCount = totalDayCount * 9 / 10;
        long dayCount = 0;
        for (int i = 0; i < sortedDateList.size(); i++) {
            PrepaymentDetailReport report = new PrepaymentDetailReport();
            Date day = DateUtil.stringToDate(sortedDateList.get(i), "yyyy-M-dd");
            Date previousDay = i == 0 ? firstDay : DateUtil.stringToDate(sortedDateList.get(i - 1), "yyyy-M-dd");
            dayCount += (day.getTime() - previousDay.getTime()) / (3600 * 1000 * 24);

            report.date = day;

            report.expectedConsumedBalance = prepayment.amount.subtract(prepayment.amount.divide(
                    new BigDecimal(totalDayCount), 2, RoundingMode.CEILING).multiply(new BigDecimal(dayCount)));
            if (report.expectedConsumedBalance.compareTo(BigDecimal.ZERO) < 0) {
                report.expectedConsumedBalance = BigDecimal.ZERO;
            }
            report.expectedSoldBalance = prepayment.amount.subtract(prepayment.amount.divide(
                    new BigDecimal(expectedDayCount), 2, RoundingMode.CEILING).multiply(new BigDecimal(dayCount)));
            if (report.expectedSoldBalance.compareTo(BigDecimal.ZERO) < 0) {
                report.expectedSoldBalance = BigDecimal.ZERO;
            }
            report.availableBalance = prepayment.amount.subtract(report.consumed);
            if (report.date.before(new Date())) {

                report.consumed = ECoupon.findConsumedByDay(prepayment.supplier.id, previousDay, day);
                consumedAmount = consumedAmount.add(report.consumed);
                if (prepayment.amount.compareTo(soldAmount) <= 0) {
                    report.consumedBalance = BigDecimal.ZERO;
                } else {
                    report.consumedBalance = prepayment.amount.subtract(consumedAmount);
                }
                report.sold = OrderItems.findSoldByDay(prepayment.supplier.id, previousDay, day);
                if (prepayment.amount.compareTo(soldAmount) <= 0) {
                    report.soldBalance = BigDecimal.ZERO;
                } else {
                    report.soldBalance = prepayment.amount.subtract(soldAmount);
                }
                soldAmount = soldAmount.add(report.sold);

            } else {
                report.soldBalance = lastSoldBalance;
                report.consumedBalance = lastConsumedBalance;
            }
            lastSoldBalance = report.soldBalance;
            lastConsumedBalance = report.consumedBalance;
            reportMap.put(sortedDateList.get(i), report);
        }
        return reportMap;
    }

}
