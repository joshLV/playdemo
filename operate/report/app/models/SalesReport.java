package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
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


    /**
     * 毛利率
     */
    public BigDecimal grossMargin;
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
     * 消费金额汇总
     */
    public BigDecimal totalConsumed;

    /**
     * 刷单金额
     */
    public BigDecimal cheatedOrderAmount;

    /**
     * 刷单量
     */
    public Long cheatedOrderNum;

    /**
     * 刷单看陈本
     */
    public BigDecimal cheatedOrderCost;

    /**
     * 退款成本
     */
    public BigDecimal refundCost;

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
    public SalesReport(OperateUser operateUser, Long buyNumber,
                       BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
    }

    //from resaler
    public SalesReport(OperateUser operateUser, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.operateUser = operateUser;
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

    //refund and consumed ecoupon
    public SalesReport(OperateUser operateUser, BigDecimal amount, Goods goods, ECouponStatus status) {
        this.operateUser = operateUser;
        if (status == ECouponStatus.REFUND) {
            this.refundAmount = amount;
        } else if (status == ECouponStatus.CONSUMED) {
            this.consumedAmount = amount;
        }

        this.goods = goods;
    }
    //--------人效报表_end---------------------------//

    //--------销售报表_begin---------------------------//
    /**
     * 渠道成本
     */
    public BigDecimal channelCost;


    public SalesReport(Goods goods, BigDecimal originalPrice, Long buyNumber,
                       BigDecimal totalAmount, BigDecimal avgSalesPrice,
                       BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount
            , BigDecimal totalCost) {
        this.goods = goods;
        this.originalPrice = originalPrice;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
    }

    //from resaler
    public SalesReport(Goods goods, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.goods = goods;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //cheated order
    public SalesReport(Goods goods, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.goods = goods;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
    }


    //refund ecoupon
    public SalesReport(BigDecimal refundAmount, Goods goods, BigDecimal refundCost) {
        this.refundAmount = refundAmount;
        this.goods = goods;
        this.refundCost = refundCost;

    }

    //consumedAt ecoupon
    public SalesReport(Goods goods, BigDecimal consumedAmount) {
        this.goods = goods;
        this.consumedAmount = consumedAmount;
    }

    public SalesReport(Long buyNumber, BigDecimal originalAmount) {
        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public SalesReport(BigDecimal totalConsumed, BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit) {
        this.totalConsumed = totalConsumed;
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> query(SalesReportCondition condition) {
        //paidAt
        String sql = "select new models.SalesReport(r.goods,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                " )" +
                " from OrderItems r ";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();

//        for (SalesReport s : paidResultList) {
//            System.out.println("sales>>>" + s.totalAmount);
//            System.out.println("cost>>" + s.totalCost);
//        }

        //from resaler
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b";
        groupBy = " group by r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }


        List<SalesReport> paidResalerResultList = query.getResultList();

//        System.out.println("size>>>" + paidResalerResultList.size());
//
//        for (SalesReport s : paidResalerResultList) {
//            System.out.println("ratio>>>>"+s.ratio);
//        }

        //cheated order
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r and ";
        groupBy = " group by r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<SalesReport> cheatedOrderResultList = query.getResultList();
//        for (SalesReport s : cheatedOrderResultList) {
//            System.out.println("");
//            System.out.println("sales>>>" + s.cheatedOrderAmount);
//            System.out.println("cost>>" + s.cheatedOrderCost);
//        }

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(sum(e.refundPrice),e.orderItems.goods,sum(r.originalPrice)) " +
                " from ECoupon e,OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();
//        for (SalesReport s : refundList) {
//            System.out.println("");
//            System.out.println("sales>>>" + s.refundAmount);
//            System.out.println("cost>>" + s.refundCost);
//        }

        //consumedAt
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<SalesReport> consumedResultList = query.getResultList();

        Map<Goods, SalesReport> map = new HashMap<>();

        //merge
        for (SalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (SalesReport cheatedItem : cheatedOrderResultList) {
            SalesReport item = map.get(getReportKey(cheatedItem));
            if (item == null) {
                Goods goods = Goods.findById(cheatedItem.goods.id);
                cheatedItem.originalPrice = goods.originalPrice;
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.refundAmount);
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
//                System.out.println("sales>>>" + item.totalAmount);

                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).subtract(cheatedItem.cheatedOrderCost);
            }
        }

        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).subtract(refundItem.refundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount).subtract(item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).subtract(item.refundCost == null ? BigDecimal.ZERO : item.refundCost);
//                System.out.println("item.totalAmount>>>" + item.totalAmount);
//                System.out.println("item.reufnd>>>" + item.refundAmount);
//                System.out.println("item.cheat>>>" + item.cheatedOrderAmount);
//                System.out.println("item.totalcost>>>" + item.totalCost);
//                System.out.println("item.refundcost>>>" + item.refundCost);
//                System.out.println("item.cheatcost>>>" + item.cheatedOrderCost);
            }
        }

        for (SalesReport consumedItem : consumedResultList) {
            SalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                Goods goods = Goods.findById(consumedItem.goods.id);
                consumedItem.originalPrice = goods.originalPrice;
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }

        //merge from resaler if commissionRatio
        for (SalesReport resalerItem : paidResalerResultList) {
            SalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
//                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
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
        String sql = "select new models.SalesReport(o,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Supplier s,OperateUser o";
        String groupBy = " group by s.salesId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();

        //from resaler
        sql = "select new models.SalesReport(ou,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.refundPrice),e.orderItems.goods,e.status) from ECoupon e,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();
        Map<OperateUser, SalesReport> map = new HashMap<>();
        //merge
        for (SalesReport paidItem : paidResultList) {
            map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
        }

        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item != null) {
                item.refundAmount = refundItem.refundAmount;
            }
        }
        //取得消费的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.salePrice),e.orderItems.goods,e.status) from ECoupon e,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> consumedList = query.getResultList();

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
        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal totalConsumed = BigDecimal.ZERO;

        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            totalConsumed = totalConsumed.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new SalesReport(totalConsumed.setScale(2), totalAmount, refundAmount, netSalesAmount, grossMargin, channelCost, profit);
    }


    private static Goods getReportKey(SalesReport refundItem) {
        return refundItem.goods;
    }

    private static OperateUser getReportKeyOfPeopleEffect(SalesReport refundItem) {
        return refundItem.operateUser;
    }
}
