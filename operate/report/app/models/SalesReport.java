package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.Query;

/**
 * 销售报表
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午4:54
 */
public class SalesReport {
    public Goods goods;
    public BigDecimal avgSalesPrice;
    public BigDecimal grossMargin;       //毛利率
    public BigDecimal originalPrice;
    public Long buyNumber;
    public BigDecimal totalAmount;
    public String reportDate;
    public BigDecimal refundAmount;

    public SalesReport(Goods goods, BigDecimal originalPrice, Long buyNumber,
                       BigDecimal totalAmount, BigDecimal avgSalesPrice, BigDecimal grossMargin) {
        this.goods = goods;
        this.originalPrice = originalPrice;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;

    }

    //refund ecoupon
    public SalesReport(BigDecimal refundAmount, Goods goods) {
        this.refundAmount = refundAmount;
        this.goods = goods;

    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> query(SalesReportCondition condition) {
        //paidAt
        String sql = "select new models.SalesReport(r.goods,r.originalPrice,count(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/count(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*count(r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100)" +
                "from OrderItems r";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by count(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(sum(e.refundPrice),e.orderItems.goods) from ECoupon e ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();

        Map<Goods, SalesReport> map = new HashMap<>();

        //merge
        for (SalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
            }
        }


        List resultList = new ArrayList();
        for (Goods key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    private static Goods getReportKey(SalesReport refoundItem) {
        return refoundItem.goods;
    }

}
