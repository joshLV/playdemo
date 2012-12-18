package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.accounts.AccountType;
import models.order.Order;
import models.resale.Resaler;
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
    public BigDecimal profit;
    public BigDecimal netSalesAmount;
    public BigDecimal totalCost;
    public BigDecimal ratio;
    public BigDecimal originalAmount;

    public SalesReport(Goods goods, BigDecimal originalPrice, Long buyNumber,
                       BigDecimal totalAmount, BigDecimal avgSalesPrice,
                       BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount) {
        this.goods = goods;
        this.originalPrice = originalPrice;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
    }

    //from resaler
    public SalesReport(Goods goods, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.goods = goods;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //refund ecoupon
    public SalesReport(BigDecimal refundAmount, Goods goods) {
        this.refundAmount = refundAmount;
        this.goods = goods;

    }

    public SalesReport(Long buyNumber, BigDecimal originalAmount) {
        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public SalesReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount) {
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> query(SalesReportCondition condition) {
        //paidAt
        String sql = "select new models.SalesReport(r.goods,r.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();


        //from resaler
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-r.originalPrice*sum(r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b";
        groupBy = " group by r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResalerResultList = query.getResultList();

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
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (SalesReport resalerItem : paidResalerResultList) {
            SalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit.subtract(resalerItem.totalAmount.subtract(resalerItem.totalCost)).add(resalerItem.profit);
            }
        }

        List resultList = new ArrayList();
        for (Goods key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 取得净销售的总计
     *
     * @param resultList
     * @return
     */
    public static SalesReport getNetSummary(List<SalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new SalesReport(0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
        }
        return new SalesReport(totalAmount, refundAmount, netSalesAmount);
    }

    private static Goods getReportKey(SalesReport refoundItem) {
        return refoundItem.goods;
    }

}
