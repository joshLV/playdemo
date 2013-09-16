package models;

import com.uhuila.common.util.DateUtil;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道销售汇总商品
 * <p/>
 * User: wangjia
 * Date: 13-4-11
 * Time: 下午4:39
 */
public class ChannelSalesDailyReport implements Comparable<ChannelSalesDailyReport> {
    /**
     * 发生日期.
     */
    public String date;


    public BigDecimal salesAmount;      //销售额

    public BigDecimal refundAmount;   //退款金额

    public BigDecimal cheatedOrderAmount; //刷单金额

    public BigDecimal netSalesAmount;   //净销售额=销售金额-退款金额-刷单金额

    public String loginName;

    public ChannelSalesDailyReport(String date) {
        this.date = date;
    }

    //paidAt
    public ChannelSalesDailyReport(String date, Order order, BigDecimal netSalesAmount) {
        this.date = date;
        this.salesAmount = netSalesAmount;
        this.netSalesAmount = netSalesAmount;
        this.loginName = order.getResaler().loginName;
    }

    //refundAt
    public ChannelSalesDailyReport(String date, Order order, BigDecimal refundAmount, Goods goods) {
        this.date = date;
        this.refundAmount = refundAmount;
        this.loginName = order.getResaler().loginName;
    }

    //cheatedOrder
    public ChannelSalesDailyReport(String date, BigDecimal cheatedOrderAmount, Order order, OrderItems orderItems) {
        this.date = date;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.loginName = order.getResaler().loginName;
    }


    /**
     * 渠道销售汇总日报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelSalesDailyReport> query(
            ChannelSalesDailyReportCondition condition) {
        //算出券和实物销售额 paidAt  (ecoupon + real)
        String sql = "select new models.ChannelSalesDailyReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),min(o),sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Order o where r.order=o ";
        String groupBy = " group by TO_DAYS(r.order.paidAt), r.order.userId";
        Query query = JPA.em()
                .createQuery(sql + condition.getPaidAtFilter() + groupBy);
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelSalesDailyReport> paidResultList = query.getResultList();

        //算出券退款金额  refundAt (ecoupon)
        sql = "select new models.ChannelSalesDailyReport(str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt)),e.order,sum(e.salePrice),e.goods " +
                ") from ECoupon e ";
        groupBy = " group by  TO_DAYS(e.refundAt),e.order.userId ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundAtFilter() + groupBy);

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelSalesDailyReport> refundResultList = query.getResultList();

        //算出券的刷单金额 cheatedOrder
        sql = "select new models.ChannelSalesDailyReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),sum(r.salePrice-r.rebateValue/r.buyNumber),r.order,r" +
                ") " +
                " from OrderItems r, ECoupon e where e.orderItems=r and ";
        groupBy = " group by TO_DAYS(r.order.paidAt), r.order.userId ";
        query = JPA.em()
                .createQuery(sql + condition.getCheatedOrderFilter() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelSalesDailyReport> cheatedOrderResultList = query.getResultList();


        Map<String, ChannelSalesDailyReport> map = new HashMap<>();

        //merge
        for (ChannelSalesDailyReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ChannelSalesDailyReport refundItem : refundResultList) {
            ChannelSalesDailyReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount == null ? BigDecimal.ZERO : refundItem.refundAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                System.out.println("item.loginName = " + item.loginName);
                System.out.println("item.salesAmount = " + item.salesAmount);

                item.refundAmount = refundItem.refundAmount;
                System.out.println("item.refundAmount = " + item.refundAmount);

                item.netSalesAmount = (item.salesAmount == null ? BigDecimal.ZERO : item.salesAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
                System.out.println("item.netSalesAmount = " + item.netSalesAmount);
                System.out.println("");

            }
        }

        for (ChannelSalesDailyReport cheatedItem : cheatedOrderResultList) {
            ChannelSalesDailyReport item = map.get(getReportKey(cheatedItem));
            if (item == null) {
                item.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.refundAmount == null ? BigDecimal.ZERO : cheatedItem.refundAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.netSalesAmount = (item.salesAmount == null ? BigDecimal.ZERO : item.salesAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }

        List<ChannelSalesDailyReport> resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);
        }
        Collections.sort(tempString);

        for (String key : tempString) {
            resultList.add(map.get(key));
        }
        return resultList;

    }

    public static String getReportKey(ChannelSalesDailyReport refoundItem) {
        return String.valueOf(refoundItem.date + refoundItem.loginName);
    }



    public static List<String> generateDateList(ChannelSalesDailyReportCondition condition) {
        Date date = condition.beginAt;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
        List<String> dateList = new ArrayList<>();
        long oneDay = 1000L * 60 * 60 * 24;
        do {
            dateList.add(df.format(date));
            date = new Date(date.getTime() + oneDay);
        } while (date.compareTo(condition.endAt) <= 0);
        return dateList;
    }

    @Override
    public int compareTo(ChannelSalesDailyReport arg) {
        return DateUtil.stringToDate(arg.date == null ? "" : arg.date, "yyyy-MM-dd").compareTo(DateUtil.stringToDate(this.date == null ? "" : this.date, "yyyy-MM-dd"));

    }
}
