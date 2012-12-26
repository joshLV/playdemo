package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售报表
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午4:54
 */
public class SalesReport {
    public Goods goods;
    /**
     * 平均售价
     */
    public BigDecimal avgSalesPrice;
    public BigDecimal grossMargin;       //毛利率
    /**
     * 进价
     */
    public BigDecimal originalPrice;
    /**
     * 销售数量
     */
    public Long buyNumber;
    /**
     * 售出总金额
     */
    public BigDecimal totalAmount;
    public String reportDate;
    /**
     * 退款金额
     */
    public BigDecimal refundAmount;

    /**
     * 消费金额
     */
    public BigDecimal consumedAmount;

    /**
     * 利润
     */
    public BigDecimal profit;
    public BigDecimal netSalesAmount;
    public BigDecimal totalCost;
    public BigDecimal ratio;
    public BigDecimal originalAmount;
    /**
     * 总销售数量
     */
    public Long totalBuyNumber;

    public OperateUser operateUser;

    //--------人效报表_begin---------------------------//
    public SalesReport(Supplier supplier, Long buyNumber,
                       BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount) {
        this.operateUser = OperateUser.findById(supplier.salesId);
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
    }

    //from resaler
    public SalesReport(Supplier supplier, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.operateUser = OperateUser.findById(supplier.salesId);
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    public SalesReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal consumedAmount, BigDecimal profit, Long totalBuyNumber) {
        this.totalAmount = totalAmount;
        this.consumedAmount = consumedAmount;
        this.profit = profit;
        this.refundAmount = refundAmount;
        this.totalBuyNumber = totalBuyNumber;
    }

    //--------销售报表_begin---------------------------//
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
        groupBy = " group by r.goods.id,b ";
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
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
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
     * 取得按销售人员统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> queryPeopleEffectData(SalesReportCondition condition) {
        //毛利率= （总的销售额-总成本（进价*数量）/总销售额

        //paidAt
        String sql = "select new models.SalesReport(s,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Supplier s";
        String groupBy = " group by s.salesId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();

        //from resaler
        sql = "select new models.SalesReport(s,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-r.originalPrice*sum(r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(sum(e.refundPrice),e.orderItems.goods) from ECoupon e,Supplier s ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();

        //取得消费的数据 ecoupon
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods) from ECoupon e,Supplier s ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> consumedList = query.getResultList();

        Map<OperateUser, SalesReport> map = new HashMap<>();

        //merge
        for (SalesReport paidItem : paidResultList) {
            map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
        }

        for (SalesReport refundItem : refundList) {
            System.out.println(refundItem.operateUser+"----");
            SalesReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item != null) {
                item.refundAmount = refundItem.refundAmount;
            }
        }
        for (SalesReport consumedItem : consumedList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(consumedItem));
            if (item != null) {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }
        //merge from resaler if commissionRatio
        for (SalesReport resalerItem : paidResalerResultList) {

            SalesReport item = map.get(getReportKeyOfPeopleEffect(resalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount.subtract(resalerItem.totalCost)).add(resalerItem.profit);
            }
        }

        List resultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
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
    public static SalesReport getPeopleEffectSummary(List<SalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new SalesReport(0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netProfit = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal consumedAmount = BigDecimal.ZERO;
        Long totalBuyNumber = 0L;
        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            consumedAmount = consumedAmount.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            totalBuyNumber = totalBuyNumber + item.buyNumber;
            netProfit = netProfit.add(item.profit).setScale(3);
        }
        return new SalesReport(totalAmount, refundAmount, consumedAmount, netProfit, totalBuyNumber);
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
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
        }
        return new SalesReport(totalAmount, refundAmount, netSalesAmount);
    }

    private static Goods getReportKey(SalesReport refundItem) {
        return refundItem.goods;
    }

    private static OperateUser getReportKeyOfPeopleEffect(SalesReport refundItem) {
        return refundItem.operateUser;
    }
}
