package models;

import models.operator.OperateUser;
import models.order.ECouponStatus;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
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
public class SalesReport implements Comparable<SalesReport> {
    public Goods goods;
    /**
     * 平均售价
     */
    public BigDecimal avgSalesPrice;

    public String orderBy;

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
     * 总销售额佣金成本
     */
    public BigDecimal totalAmountCommissionAmount = BigDecimal.ZERO;

    /**
     * 退款佣金成本
     */
    public BigDecimal refundCommissionAmount = BigDecimal.ZERO;

    /**
     * 刷单佣金成本
     */
    public BigDecimal cheatedOrderCommissionAmount = BigDecimal.ZERO;


    /**
     * 从分销销来 的退款数量
     */
    public Long refundNum;


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
                       BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount, BigDecimal totalCost) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
    }

    public SalesReport(OperateUser operateUser) {
        this.operateUser = operateUser;
        this.buyNumber = 0l;
        this.totalAmount = BigDecimal.ZERO;
        this.consumedAmount = BigDecimal.ZERO;
        this.refundAmount = BigDecimal.ZERO;
        this.cheatedOrderAmount = BigDecimal.ZERO;
        this.grossMargin = BigDecimal.ZERO;
        this.profit = BigDecimal.ZERO;
        this.netSalesAmount = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    //from resaler
    public SalesReport(BigDecimal totalAmountCommissionAmount, BigDecimal ratio, OperateUser operateUser) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
    }

    public SalesReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal consumedAmount, BigDecimal profit, BigDecimal grossMargin, Long totalBuyNumber, BigDecimal netSalesAmount) {
        this.totalAmount = totalAmount;
        this.consumedAmount = consumedAmount;
        this.profit = profit;
        this.refundAmount = refundAmount;
        this.totalBuyNumber = totalBuyNumber;
        this.grossMargin = grossMargin;
        this.netSalesAmount = netSalesAmount;
    }

    //refund from resaler
    public SalesReport(OperateUser operateUser, Long refundNum, BigDecimal refundCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundNum = refundNum;
    }

    //refund and consumed ecoupon
    public SalesReport(OperateUser operateUser, BigDecimal amount, BigDecimal refundCost, ECouponStatus status) {
        this.operateUser = operateUser;
        if (status == ECouponStatus.REFUND) {
            this.refundAmount = amount;
            this.refundCost = refundCost;
        } else if (status == ECouponStatus.CONSUMED) {
            this.consumedAmount = amount;
        }
    }

    //cheated order from resaler
    public SalesReport(OperateUser operateUser, BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //cheated order
    public SalesReport(OperateUser operateUser, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.operateUser = operateUser;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
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

    //padiAt from resaler
    public SalesReport(Goods goods, BigDecimal totalAmountCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.goods = goods;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
    }

    //cheated order from resaler
    public SalesReport(BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio, Goods goods) {
        this.ratio = ratio;
        this.goods = goods;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //refund from resaler
    public SalesReport(BigDecimal refundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum) {
        this.ratio = ratio;
        this.goods = goods;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundNum = refundNum;
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
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit, BigDecimal cheatedOrderAmount) {
        this.totalConsumed = totalConsumed;
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
        this.cheatedOrderAmount = cheatedOrderAmount;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> query(SalesReportCondition condition, String orderBy) {
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

        //paidAt from resaler
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b";
        groupBy = " group by r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResalerResultList = query.getResultList();

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

        //cheated order from resaler
        sql = "select new models.SalesReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,r.goods)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e where e.orderItems=r and";
        groupBy = " group by r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResaler() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> cheatedOrderResalerResultList = query.getResultList();


        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods,sum(r.originalPrice)) " +
                " from ECoupon e,OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();

        //refund from resaler
        sql = "select new models.SalesReport(sum(e.salePrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResaler() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundResalerResultList = query.getResultList();

        //consumedAt
        sql = "select new models.SalesReport(r.goods,sum(r.salePrice-r.rebateValue/r.buyNumber)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        groupBy = " group by r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
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
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderAmount);
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost);
            }
        }
        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost);
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
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (SalesReport resalerItem : paidResalerResultList) {
            SalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        totalCommission = BigDecimal.ZERO;
        for (SalesReport cheatedResalerItem : cheatedOrderResalerResultList) {
            SalesReport item = map.get(getReportKey(cheatedResalerItem));
            if (item == null) {
                map.put(getReportKey(cheatedResalerItem), cheatedResalerItem);
            } else {
                totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
                totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
                item.cheatedOrderCommissionAmount = totalCommission;

                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }
        totalCommission = BigDecimal.ZERO;
        for (SalesReport refundResalerItem : refundResalerResultList) {
            SalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        List resultList = new ArrayList();
        for (Goods key : map.keySet()) {
            map.get(key).orderBy = orderBy;
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
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber))" +
                " from OrderItems r,Supplier s,OperateUser o";
        String groupBy = " group by s.salesId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResultList = query.getResultList();
        //cheated order
        sql = "select new models.SalesReport(ou,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e ,Supplier s,OperateUser ou";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderOfPeopleEffect() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<SalesReport> cheatedOrderResultList = query.getResultList();

        //cheated order from resaler
        sql = "select new models.SalesReport(ou,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResalerOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> cheatedOrderResalerResultList = query.getResultList();
        //paidAt from resaler
        sql = "select new models.SalesReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,ou)" +
                " from OrderItems r,Order o,Resaler b,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.salePrice),sum(r.originalPrice),e.status) from ECoupon e,OrderItems r,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();

        //取得消费的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.salePrice),sum(r.originalPrice),e.status) from ECoupon e,OrderItems r,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by sum(e.salePrice) desc");


        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> consumedList = query.getResultList();

        //refund from resaler
        sql = "select new models.SalesReport(ou,sum(r),sum(e.salePrice)*b.commissionRatio/100,b.commissionRatio) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResalerOfPeopleEffect() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundResalerResultList = query.getResultList();


        Map<OperateUser, SalesReport> map = new HashMap<>();
        //merge
        for (SalesReport paidItem : paidResultList) {
            map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
        }


        for (SalesReport cheatedItem : cheatedOrderResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(cheatedItem));
            if (item == null) {
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.refundAmount);
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
                map.put(getReportKeyOfPeopleEffect(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost);
            }
        }
        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item != null) {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost);

            } else {
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                map.put(getReportKeyOfPeopleEffect(refundItem), refundItem);
            }
        }

        for (SalesReport consumedItem : consumedList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(consumedItem));
            if (item != null) {
                item.consumedAmount = consumedItem.consumedAmount;
            } else {
                map.put(getReportKeyOfPeopleEffect(consumedItem), consumedItem);
            }

        }
        BigDecimal totalCommission = BigDecimal.ZERO;
        //merge from resaler if commissionRatio
        for (SalesReport resalerItem : paidResalerResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(resalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }


        for (SalesReport cheatedResalerItem : cheatedOrderResalerResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(cheatedResalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(cheatedResalerItem), cheatedResalerItem);
            } else {
                totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
                totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
                item.cheatedOrderCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }


        for (SalesReport refundResalerItem : refundResalerResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(refundResalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        List resultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
            resultList.add(map.get(key));
        }
        condition.sort(resultList);
        return resultList;
    }


    public static List<SalesReport> queryNoContributionPeopleEffectData(SalesReportCondition condition,Boolean hasSeeReportProfitRight) {

        String sql = "select new models.SalesReport(o)" +
                " from Goods g,Supplier s,OperateUser o" +
                "  where g.supplierId =s.id and s.deleted=0 and s.salesId=o.id and g.isLottery=false ";

         Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(condition.jobNumber)) {
            sql=sql.concat("and o.jobNumber=:jobNumber");
            params.put("jobNumber",condition.jobNumber);
        }
        if (!hasSeeReportProfitRight) {
            sql=sql.concat("and o.id = :salesId");
            params.put("salesId",condition.salesId);
        }
        if (StringUtils.isNotBlank(condition.userName)) {
            sql=sql.concat("and o.userName like :userName" );
            params.put("userName","%"+condition.userName.trim()+"%");
        }


        String groupBy = " group by s.salesId";

        Query query = JPA.em()
                .createQuery(sql + groupBy);
        for (String param : params.keySet()) {
            query.setParameter(param, params.get(param));
        }

        List<SalesReport> resultList = query.getResultList();

        return resultList;
    }


    /**
     * 取得人效报表的金额总计
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
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        Long totalBuyNumber = 0L;
        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            consumedAmount = consumedAmount.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            totalBuyNumber = totalBuyNumber + (item.buyNumber == null ? 0l : item.buyNumber);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            netProfit = netProfit.add(item.profit == null ? BigDecimal.ZERO : item.profit).setScale(2, BigDecimal.ROUND_UP);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            totalSalePrice = totalSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
        }
        if (totalSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totalSalePrice.subtract(totalCost).divide(totalSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new SalesReport(totalAmount, refundAmount, consumedAmount, netProfit, grossMargin, totalBuyNumber, netSalesAmount);
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
        BigDecimal cheatedOrderAmount = BigDecimal.ZERO;

        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            totalConsumed = totalConsumed.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            cheatedOrderAmount = cheatedOrderAmount.add(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new SalesReport(totalConsumed.setScale(2, 4), totalAmount.setScale(2, 4), refundAmount.setScale(2, 4), netSalesAmount.setScale(2, 4), grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4), cheatedOrderAmount.setScale(2, 4));
    }


    private static Goods getReportKey(SalesReport refundItem) {
        return refundItem.goods;
    }

    private static OperateUser getReportKeyOfPeopleEffect(SalesReport refundItem) {
        return refundItem.operateUser;
    }

    @Override
    public int compareTo(SalesReport arg) {
//         后面一位：1是升序，2是降序
        switch (this.orderBy) {
            case "02":
                return arg.goods.code.compareTo(this.goods.code);
            case "01":
                return this.goods.code.compareTo(arg.goods.code);
            case "12":
                return arg.goods.shortName.compareTo(this.goods.shortName);
            case "11":
                return this.goods.shortName.compareTo(arg.goods.shortName);
            case "22":
                return arg.originalPrice.compareTo(this.originalPrice);
            case "21":
                return this.originalPrice.compareTo(arg.originalPrice);
            case "32":
                return (arg.avgSalesPrice == null ? BigDecimal.ZERO : arg.avgSalesPrice).compareTo(this.avgSalesPrice == null ? BigDecimal.ZERO : this.avgSalesPrice);
            case "31":
                return (this.avgSalesPrice == null ? BigDecimal.ZERO : this.avgSalesPrice).compareTo(arg.avgSalesPrice == null ? BigDecimal.ZERO : arg.avgSalesPrice);
            case "42":
                return (arg.buyNumber == null ? BigDecimal.ZERO : BigDecimal.valueOf(arg.buyNumber)).compareTo(this.buyNumber == null ? BigDecimal.ZERO : BigDecimal.valueOf(this.buyNumber));
            case "41":
                return (this.buyNumber == null ? BigDecimal.ZERO : BigDecimal.valueOf(this.buyNumber)).compareTo(arg.buyNumber == null ? BigDecimal.ZERO : BigDecimal.valueOf(arg.buyNumber));
            case "52":
                return (arg.totalAmount == null ? BigDecimal.ZERO : arg.totalAmount).compareTo(this.totalAmount == null ? BigDecimal.ZERO : this.totalAmount);
            case "51":
                return (this.totalAmount == null ? BigDecimal.ZERO : this.totalAmount).compareTo(arg.totalAmount == null ? BigDecimal.ZERO : arg.totalAmount);
            case "62":
                return (arg.cheatedOrderAmount == null ? BigDecimal.ZERO : arg.cheatedOrderAmount).compareTo(this.cheatedOrderAmount == null ? BigDecimal.ZERO : this.cheatedOrderAmount);
            case "61":
                return (this.cheatedOrderAmount == null ? BigDecimal.ZERO : this.cheatedOrderAmount).compareTo(arg.cheatedOrderAmount == null ? BigDecimal.ZERO : arg.cheatedOrderAmount);
            case "72":
                return (arg.refundAmount == null ? BigDecimal.ZERO : arg.refundAmount).compareTo(this.refundAmount == null ? BigDecimal.ZERO : this.refundAmount);
            case "71":
                return (this.refundAmount == null ? BigDecimal.ZERO : this.refundAmount).compareTo(arg.refundAmount == null ? BigDecimal.ZERO : arg.refundAmount);
            case "82":
                return (arg.consumedAmount == null ? BigDecimal.ZERO : arg.consumedAmount).compareTo(this.consumedAmount == null ? BigDecimal.ZERO : this.consumedAmount);
            case "81":
                return (this.consumedAmount == null ? BigDecimal.ZERO : this.consumedAmount).compareTo(arg.consumedAmount == null ? BigDecimal.ZERO : arg.consumedAmount);
            case "92":
                return (arg.netSalesAmount == null ? BigDecimal.ZERO : arg.netSalesAmount).compareTo(this.netSalesAmount == null ? BigDecimal.ZERO : this.netSalesAmount);
            case "91":
                return (this.netSalesAmount == null ? BigDecimal.ZERO : this.netSalesAmount).compareTo(arg.netSalesAmount == null ? BigDecimal.ZERO : arg.netSalesAmount);
            case "102":
                return (arg.grossMargin == null ? BigDecimal.ZERO : arg.grossMargin).compareTo(this.grossMargin == null ? BigDecimal.ZERO : this.grossMargin);
            case "101":
                return (this.grossMargin == null ? BigDecimal.ZERO : this.grossMargin).compareTo(arg.grossMargin == null ? BigDecimal.ZERO : arg.grossMargin);
            case "112":
                return (arg.profit == null ? BigDecimal.ZERO : arg.profit).compareTo(this.profit == null ? BigDecimal.ZERO : this.profit);
            case "111":
                return (this.profit == null ? BigDecimal.ZERO : this.profit).compareTo(arg.profit == null ? BigDecimal.ZERO : arg.profit);
        }
        return 0;
    }
}
