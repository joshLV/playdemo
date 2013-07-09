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
     * 本期购买，本期未消费退款金额
     */
    public BigDecimal salesRefundAmount;
    public BigDecimal salesRefundCost;


    /**
     * 本期之前购买，本期未消费退款金额
     */
    public BigDecimal previousSalesRefundAmount;
    public BigDecimal previousSalesRefundCost;

    /**
     * 本期消费，本期消费退款
     */
    public BigDecimal consumedRefundAmount;
    public BigDecimal consumedRefundCost;


    /**
     * 本期之前消费，本期消费退款
     */
    public BigDecimal previousConsumedRefundAmount;
    public BigDecimal previousConsumedRefundCost;


    /**
     * 消费金额
     */
    public BigDecimal consumedAmount;


    public BigDecimal consumedCost;

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
     * 刷单成本
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
     * 本期购买，本期未消费退款佣金成本
     */
    public BigDecimal salesRefundCommissionAmount;

    /**
     * 本期之前购买，本期未消费退款佣金成本
     */
    public BigDecimal previousSalesRefundCommissionAmount;

    /**
     * 本期消费，本期消费退款佣金成本
     */
    public BigDecimal consumedRefundCommissionAmount;

    /**
     * 本期之前消费，本期消费退款佣金成本
     */
    public BigDecimal previousConsumedRefundCommissionAmount;

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
    public BigDecimal netCost;
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
                       BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount, BigDecimal totalCost, BigDecimal totalAmountCommissionAmount) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
        this.netCost = totalCost;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
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
        this.netCost = BigDecimal.ZERO;
    }

    //from resaler
    public SalesReport(BigDecimal totalAmountCommissionAmount, BigDecimal ratio, OperateUser operateUser) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
    }

    public SalesReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal consumedAmount, BigDecimal consumedCost,
                       BigDecimal profit, BigDecimal grossMargin, Long totalBuyNumber,
                       BigDecimal netSalesAmount, BigDecimal netCost) {
        this.totalAmount = totalAmount;
        this.consumedAmount = consumedAmount;
        this.consumedCost = consumedCost;
        this.profit = profit;
        this.refundAmount = refundAmount;
        this.totalBuyNumber = totalBuyNumber;
        this.grossMargin = grossMargin;
        this.netSalesAmount = netSalesAmount;
        this.netCost = netCost;
    }

    //refund from resaler
    public SalesReport(OperateUser operateUser, Long refundNum, BigDecimal refundCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundNum = refundNum;
    }

    //refund and consumed ecoupon
    public SalesReport(OperateUser operateUser, BigDecimal amount, BigDecimal refundCost, ECouponStatus status, BigDecimal refundCommissionAmount) {
        this.operateUser = operateUser;
        if (status == ECouponStatus.REFUND) {
            this.refundAmount = amount;
            this.refundCost = refundCost;
            this.refundCommissionAmount = refundCommissionAmount;
        } else if (status == ECouponStatus.CONSUMED) {
            this.consumedAmount = amount;
            this.consumedCost = refundCost;
        }
    }

    //cheated order from resaler
    public SalesReport(OperateUser operateUser, BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //cheated order
    public SalesReport(OperateUser operateUser, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost, BigDecimal cheatedOrderCommissionAmount) {
        this.operateUser = operateUser;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;

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
        this.netCost = totalCost;
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


    //cheated order
    public SalesReport(Goods goods, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.goods = goods;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
    }


    //refund ecoupon  本期购买，本期未消费退款
    public SalesReport(BigDecimal salesRefundAmount, Goods goods, BigDecimal salesRefundCost) {
        this.salesRefundAmount = salesRefundAmount;
        this.goods = goods;
        this.salesRefundCost = salesRefundCost;
    }

    //refund from resaler  本期购买，本期未消费退款
    public SalesReport(BigDecimal salesRefundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum) {
        this.ratio = ratio;
        this.goods = goods;
        this.salesRefundCommissionAmount = salesRefundCommissionAmount;
        this.refundNum = refundNum;
    }

    //refund ecoupon 本期之前购买，本期未消费退款
    public SalesReport(BigDecimal previousSalesRefundAmount, Goods goods, BigDecimal previousSalesRefundCost, Goods goods1) {
        this.previousSalesRefundAmount = previousSalesRefundAmount;
        this.goods = goods;
        this.previousSalesRefundCost = previousSalesRefundCost;
    }

    //refund from resaler 本期之前购买，本期未消费退款
    public SalesReport(BigDecimal previousSalesRefundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum,
                       Goods goods1) {
        this.ratio = ratio;
        this.goods = goods;
        this.previousSalesRefundCommissionAmount = previousSalesRefundCommissionAmount;
        this.refundNum = refundNum;
    }

    //refund ecoupon 本期消费，本期消费退款
    public SalesReport(BigDecimal consumedRefundAmount, Goods goods, BigDecimal consumedRefundCost, Goods goods1,
                       Goods goods2) {
        this.consumedRefundAmount = consumedRefundAmount;
        this.goods = goods;
        this.consumedRefundCost = consumedRefundCost;
    }

    //refund from resaler 本期消费，本期消费退款
    public SalesReport(BigDecimal consumedRefundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum,
                       Goods goods1, Goods goods2) {
        this.ratio = ratio;
        this.goods = goods;
        this.consumedRefundCommissionAmount = consumedRefundCommissionAmount;
        this.refundNum = refundNum;
    }

    //refund ecoupon 本期之前消费，本期消费退款
    public SalesReport(BigDecimal previousConsumedRefundAmount, Goods goods, BigDecimal previousConsumedRefundCost, Goods goods1,
                       Goods goods2, Goods goods3) {
        this.previousConsumedRefundAmount = previousConsumedRefundAmount;
        this.goods = goods;
        this.previousConsumedRefundCost = previousConsumedRefundCost;
    }

    //refund from resaler 本期之前消费，本期消费退款
    public SalesReport(BigDecimal previousConsumedRefundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum,
                       Goods goods1, Goods goods2, Goods good3) {
        this.ratio = ratio;
        this.goods = goods;
        this.previousConsumedRefundCommissionAmount = previousConsumedRefundCommissionAmount;
        this.refundNum = refundNum;
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

    public SalesReport(BigDecimal totalConsumed, BigDecimal totalAmount, BigDecimal salesRefundAmount,
                       BigDecimal previousSalesRefundAmount, BigDecimal consumedRefundAmount, BigDecimal previousConsumedRefundAmount,
                       BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit, BigDecimal cheatedOrderAmount) {
        this.totalConsumed = totalConsumed;
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.salesRefundAmount = salesRefundAmount;
        this.previousSalesRefundAmount = previousSalesRefundAmount;
        this.consumedRefundAmount = consumedRefundAmount;
        this.previousConsumedRefundAmount = previousConsumedRefundAmount;
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


        //取得退款的数据 ecoupon  本期购买，本期未消费退款
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods,sum(r.originalPrice)) " +
                " from ECoupon e,OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getSalesRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap2().keySet()) {
            query.setParameter(param, condition.getParamMap2().get(param));
        }

        List<SalesReport> salesRefundList = query.getResultList();

        //refund from resaler 本期购买，本期未消费退款
        sql = "select new models.SalesReport(sum(e.salePrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterSalesRefundResaler() + groupBy + " order by sum(e.salePrice) " +
                        "desc");

        for (String param : condition.getParamMap2().keySet()) {
            query.setParameter(param, condition.getParamMap2().get(param));
        }

        List<SalesReport> salesRefundResalerResultList = query.getResultList();

        //本期之前购买，本期未消费退款
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods,sum(r.originalPrice)," +
                "e.orderItems.goods) " +
                " from ECoupon e," +
                "OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getPreviousSalesRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap3().keySet()) {
            query.setParameter(param, condition.getParamMap3().get(param));
        }

        List<SalesReport> previousSalesRefundList = query.getResultList();


        //refund from resaler 本期之前购买，本期未消费退款
        sql = "select new models.SalesReport(sum(e.salePrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r),r.goods" +
                ") " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterPreviousSalesRefundResaler() + groupBy + " order by sum(e.salePrice) " +
                        "desc");

        for (String param : condition.getParamMap3().keySet()) {
            query.setParameter(param, condition.getParamMap3().get(param));
        }

        List<SalesReport> previousSalesRefundResalerResultList = query.getResultList();

        //本期消费，本期消费退款
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods,sum(r.originalPrice)," +
                "e.orderItems.goods," +
                "e.orderItems.goods" +
                ") " +
                " from ECoupon e," +
                "OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getConsumedRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap2().keySet()) {
            query.setParameter(param, condition.getParamMap2().get(param));
        }

        List<SalesReport> consumedRefundList = query.getResultList();

        //refund from resaler  本期消费，本期消费退款
        sql = "select new models.SalesReport(sum(e.salePrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r)," +
                "r.goods,r.goods" +
                ") " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedRefundResaler() + groupBy + " " +
                        "order by sum(e" +
                        ".salePrice) " +
                        "desc");

        for (String param : condition.getParamMap2().keySet()) {
            query.setParameter(param, condition.getParamMap2().get(param));
        }

        List<SalesReport> consumedRefundResalerResultList = query.getResultList();

        //本期之前消费，本期消费退款
        sql = "select new models.SalesReport(sum(e.salePrice),e.orderItems.goods,sum(r.originalPrice)," +
                "e.orderItems.goods," +
                "e.orderItems.goods,e.orderItems.goods" +
                ") " +
                " from ECoupon e," +
                "OrderItems r ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getPreviousConsumedRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap3().keySet()) {
            query.setParameter(param, condition.getParamMap3().get(param));
        }

        List<SalesReport> previousConsumedRefundList = query.getResultList();

        //refund from resaler 本期之前消费，本期消费退款
        sql = "select new models.SalesReport(sum(e.salePrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r)," +
                "r.goods," +
                "r.goods,r.goods" +
                ") " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterPreviousConsumedRefundResaler() + groupBy + " " +
                        "" +
                        "order by sum" +
                        "(e.salePrice) " +
                        "desc");

        for (String param : condition.getParamMap3().keySet()) {
            query.setParameter(param, condition.getParamMap3().get(param));
        }

        List<SalesReport> previousConsumedRefundResalerResultList = query.getResultList();

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
                cheatedItem.netCost = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderCost == null ? BigDecimal.ZERO :
                        cheatedItem
                                .cheatedOrderCost);
                if (cheatedItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    cheatedItem.grossMargin = BigDecimal.ZERO;
                } else {
                    cheatedItem.grossMargin = (cheatedItem.netSalesAmount.subtract(item.netCost)).divide(cheatedItem
                            .netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem
                        .cheatedOrderCost);
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.netCost = item.totalCost.subtract(item.cheatedOrderCost);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4,
                            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost);
            }
        }

        //本期购买，本期未消费退款
        for (SalesReport refundItem : salesRefundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.salesRefundAmount);
                refundItem.netCost = BigDecimal.ZERO.subtract(refundItem.salesRefundCost);
                if (refundItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    refundItem.grossMargin = BigDecimal.ZERO;
                } else {
                    refundItem.grossMargin = (refundItem.netSalesAmount.subtract(item.netCost)).divide(refundItem
                            .netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.salesRefundAmount).add(refundItem.salesRefundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.salesRefundAmount = refundItem.salesRefundAmount;
                item.salesRefundCost = refundItem.salesRefundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.netCost = (item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.salesRefundCost).subtract(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).setScale(2);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost);

//                        (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost);
            }
        }

        //本期之前购买，本期未消费退款
        for (SalesReport refundItem : previousSalesRefundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.previousSalesRefundAmount);
                refundItem.netCost = BigDecimal.ZERO.subtract(refundItem.previousSalesRefundCost);
                if (refundItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    refundItem.grossMargin = BigDecimal.ZERO;
                } else {
                    refundItem.grossMargin = (refundItem.netSalesAmount.subtract(refundItem.netCost)).divide(refundItem.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    refundItem.profit = BigDecimal.ZERO.subtract(refundItem.previousSalesRefundAmount).add(refundItem
                            .previousSalesRefundCost);
                }

                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.previousSalesRefundAmount = refundItem.previousSalesRefundAmount;
                item.previousSalesRefundCost = refundItem.previousSalesRefundCost;
                item.netSalesAmount = (item
                        .totalAmount == null ?
                        BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal
                        .ZERO : item
                        .cheatedOrderAmount).setScale
                        (2);
                item.netCost = (item
                        .totalCost == null ?
                        BigDecimal.ZERO : item.totalCost).subtract(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).subtract(item.previousSalesRefundCost).subtract(item.cheatedOrderCost == null ? BigDecimal
                        .ZERO : item
                        .cheatedOrderCost).setScale
                        (2);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4,
                            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item.previousSalesRefundAmount).add(item.previousSalesRefundCost == null ? BigDecimal.ZERO : item.previousSalesRefundCost);
//                        (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item
//                        .cheatedOrderAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).add(item.previousSalesRefundCost == null ? BigDecimal.ZERO : item.previousSalesRefundCost);
            }
        }


        //本期消费，本期消费退款
        for (SalesReport refundItem : consumedRefundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.consumedRefundAmount);
                refundItem.netCost = BigDecimal.ZERO.subtract(refundItem.consumedRefundCost);
                if (refundItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    refundItem.grossMargin = BigDecimal.ZERO;
                } else {
                    refundItem.grossMargin = (refundItem.netSalesAmount.subtract(refundItem.netCost)).divide(refundItem.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.consumedRefundAmount).add(refundItem
                        .consumedRefundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.consumedRefundAmount = refundItem.consumedRefundAmount;
                item.consumedRefundCost = refundItem.consumedRefundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item.previousSalesRefundAmount).subtract(item.consumedRefundAmount).subtract(item
                        .cheatedOrderAmount ==
                        null
                        ? BigDecimal
                        .ZERO : item
                        .cheatedOrderAmount).setScale
                        (2);
                item.netCost = (item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item
                        .salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).subtract(item.previousSalesRefundCost == null ? BigDecimal.ZERO : item.previousSalesRefundCost).subtract(item.consumedRefundCost).subtract(item
                        .cheatedOrderCost ==
                        null
                        ? BigDecimal
                        .ZERO : item
                        .cheatedOrderCost).setScale
                        (2);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4,
                            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.consumedRefundAmount == null ? BigDecimal.ZERO : item.consumedRefundAmount).add(item.consumedRefundAmount == null ? BigDecimal.ZERO : item.consumedRefundCost);

//                        (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item.previousSalesRefundAmount).subtract(item.consumedRefundAmount).
//                        subtract(item.cheatedOrderAmount == null ?
//                                BigDecimal.ZERO : item
//                                .cheatedOrderAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).add(item.previousSalesRefundCost == null ?
//                                BigDecimal.ZERO : item.previousSalesRefundCost).add(item.consumedRefundCost);
            }
        }

        //本期之前消费，本期消费退款
        for (SalesReport refundItem : previousConsumedRefundList) {
            SalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.previousConsumedRefundAmount);
                refundItem.netCost = BigDecimal.ZERO.subtract(refundItem.previousConsumedRefundCost);
                if (refundItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    refundItem.grossMargin = BigDecimal.ZERO;
                } else {
                    refundItem.grossMargin = (refundItem.netSalesAmount.subtract(refundItem.netCost)).divide(refundItem.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.previousConsumedRefundAmount).add(refundItem
                        .previousConsumedRefundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.previousConsumedRefundAmount = refundItem.previousConsumedRefundAmount;
                item.previousConsumedRefundCost = refundItem.previousConsumedRefundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item
                        .previousSalesRefundAmount).subtract(item.consumedRefundAmount == null ? BigDecimal.ZERO : item
                        .consumedRefundAmount).subtract(item.previousConsumedRefundAmount).subtract(item
                        .cheatedOrderAmount ==
                        null
                        ? BigDecimal
                        .ZERO : item
                        .cheatedOrderAmount).setScale
                        (2);
                item.netCost = (item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).subtract(item.previousSalesRefundCost == null ? BigDecimal.ZERO : item
                        .previousSalesRefundCost).subtract(item.consumedRefundCost == null ? BigDecimal.ZERO : item
                        .consumedRefundCost).subtract(item.previousConsumedRefundCost).subtract(item
                        .cheatedOrderCost ==
                        null
                        ? BigDecimal
                        .ZERO : item
                        .cheatedOrderCost).setScale
                        (2);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4,
                            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.previousConsumedRefundAmount == null ? BigDecimal.ZERO : item.previousConsumedRefundAmount).add(item.previousConsumedRefundCost == null ? BigDecimal.ZERO : item.previousConsumedRefundCost);


//                        (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount).subtract(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item.previousSalesRefundAmount).subtract(item
//                        .consumedRefundAmount == null ? BigDecimal.ZERO : item.consumedRefundAmount).subtract(item
//                        .previousConsumedRefundAmount).
//                        subtract(item.cheatedOrderAmount == null ?
//                                BigDecimal.ZERO : item
//                                .cheatedOrderAmount).subtract(item.previousConsumedRefundAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost).add(item.previousSalesRefundCost == null ?
//                                BigDecimal.ZERO : item.previousSalesRefundCost).add(item.consumedRefundCost == null ?
//                                BigDecimal.ZERO : item.consumedRefundCost).add(item
//                                .previousConsumedRefundCost);
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
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(resalerItem.totalAmountCommissionAmount);
//                                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
//                        .subtract(totalCommission == null ? BigDecimal.ZERO : totalCommission)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

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
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit);
                //.subtract(cheatedResalerItem.cheatedOrderCommissionAmount);

//                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
//                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }

        //本期购买，本期未消费退款
        totalCommission = BigDecimal.ZERO;
        for (SalesReport refundResalerItem : salesRefundResalerResultList) {
            SalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.salesRefundCommissionAmount == null ? BigDecimal.ZERO : item
                        .salesRefundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.salesRefundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.salesRefundCommissionAmount);
                item.salesRefundCommissionAmount = totalCommission;
//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.salesRefundCommissionAmount == null ? BigDecimal.ZERO : item.salesRefundCommissionAmount);
//
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).add(refundResalerItem.salesRefundCommissionAmount);

//                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount)
//                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount)
//                        .add(item.salesRefundCommissionAmount == null ? BigDecimal.ZERO : item.salesRefundCommissionAmount)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost)
//                        .add(item.salesRefundCost == null ? BigDecimal.ZERO : item.salesRefundCost)
//                        .add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        //本期之前购买，本期未消费退款
        totalCommission = BigDecimal.ZERO;
        for (SalesReport refundResalerItem : previousSalesRefundResalerResultList) {
            SalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.previousSalesRefundCommissionAmount == null ? BigDecimal.ZERO : item
                        .previousSalesRefundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.previousSalesRefundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.previousSalesRefundCommissionAmount);
                item.previousSalesRefundCommissionAmount = totalCommission;
//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.previousSalesRefundCommissionAmount == null ? BigDecimal.ZERO : item.previousSalesRefundCommissionAmount);
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).add(refundResalerItem.previousSalesRefundCommissionAmount);
//                        .subtract(item
//                                .previousSalesRefundAmount == null ? BigDecimal.ZERO : item
//                                .previousSalesRefundAmount).add(item.previousSalesRefundCommissionAmount == null ? BigDecimal.ZERO : item
//                                .previousSalesRefundCommissionAmount).add(item
//                                .previousSalesRefundCost == null ? BigDecimal.ZERO : item
//                                .previousSalesRefundCost);

            }
        }
        //本期消费，本期消费退款
        totalCommission = BigDecimal.ZERO;
        for (SalesReport refundResalerItem : consumedRefundResalerResultList) {
            SalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.consumedRefundCommissionAmount == null ? BigDecimal.ZERO : item
                        .consumedRefundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.consumedRefundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.consumedRefundCommissionAmount);
                item.consumedRefundCommissionAmount = totalCommission;
//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.consumedRefundCommissionAmount == null ? BigDecimal.ZERO : item.consumedRefundCommissionAmount);
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).add(refundResalerItem.consumedRefundCommissionAmount);

//
//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item
//                        .consumedRefundAmount == null ? BigDecimal.ZERO : item
//                        .consumedRefundAmount).add(item.consumedRefundCommissionAmount == null ? BigDecimal.ZERO : item
//                        .consumedRefundCommissionAmount).add(item
//                        .consumedRefundCost == null ? BigDecimal.ZERO : item
//                        .consumedRefundCost);
            }
        }

        //本期之前消费，本期消费退款
        totalCommission = BigDecimal.ZERO;
        for (SalesReport refundResalerItem : previousConsumedRefundResalerResultList) {
            SalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.previousConsumedRefundCommissionAmount == null ? BigDecimal.ZERO : item
                        .previousConsumedRefundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.previousConsumedRefundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.previousConsumedRefundCommissionAmount);
                item.previousConsumedRefundCommissionAmount = totalCommission;
//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item.previousConsumedRefundCommissionAmount == null ? BigDecimal.ZERO : item.previousConsumedRefundCommissionAmount);
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).add(refundResalerItem.previousConsumedRefundCommissionAmount);

//                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit).subtract(item
//                        .previousConsumedRefundAmount == null ? BigDecimal.ZERO : item
//                        .previousConsumedRefundAmount).add(item.previousConsumedRefundCommissionAmount == null ? BigDecimal.ZERO : item
//                        .previousConsumedRefundCommissionAmount).add(item
//                        .previousConsumedRefundCost == null ? BigDecimal.ZERO : item
//                        .previousConsumedRefundCost);
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
                ",sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.commission*r.buyNumber) )" +
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
                " ,sum(r.originalPrice),sum(r.commission*r.buyNumber)) " +
                " from OrderItems r, ECoupon e ,Supplier s,OperateUser ou";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderOfPeopleEffect() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<SalesReport> cheatedOrderResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.salePrice),sum(e.originalPrice),e.status,sum(r.commission)) from ECoupon e,OrderItems r,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesReport> refundList = query.getResultList();

        //取得消费的数据 ecoupon
        sql = "select new models.SalesReport(o,sum(e.salePrice),sum(e.originalPrice),e.status,sum(r.commission)) from ECoupon e,OrderItems r,Supplier s,OperateUser o ";
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


        for (SalesReport cheatedItem : cheatedOrderResultList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(cheatedItem));
            if (item == null) {
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount);
                cheatedItem.netCost = cheatedItem.totalCost.subtract(cheatedItem.cheatedOrderCost);
                if (cheatedItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    cheatedItem.grossMargin = BigDecimal.ZERO;
                } else {
                    cheatedItem.grossMargin = (cheatedItem.netSalesAmount.subtract(cheatedItem.netCost)).divide(cheatedItem.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).add(cheatedItem.cheatedOrderCost)
                        .add(item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount);
                map.put(getReportKeyOfPeopleEffect(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.netCost = item.totalCost.subtract(item.cheatedOrderCost);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount)
                        .add(cheatedItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderCommissionAmount);
            }
        }
        for (SalesReport refundItem : refundList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item != null) {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.netCost = (item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.refundCost).subtract(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).setScale(2);
                if (item.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    item.grossMargin = BigDecimal.ZERO;
                } else {
                    item.grossMargin = (item.netSalesAmount.subtract(item.netCost)).divide(item.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).
                        subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).
                                add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).
                                add(refundItem.refundCost == null ? BigDecimal.ZERO : refundItem.refundCost
                                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount)
                                        .add(refundItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundItem.refundCommissionAmount));

            } else {
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.netCost = BigDecimal.ZERO.subtract(refundItem.refundCost);
                if (refundItem.netSalesAmount.compareTo(BigDecimal.ZERO) == 0) {
                    refundItem.grossMargin = BigDecimal.ZERO;
                } else {
                    refundItem.grossMargin = (refundItem.netSalesAmount.subtract(refundItem.netCost)).divide(refundItem.netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount)
                        .add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount);
                map.put(getReportKeyOfPeopleEffect(refundItem), refundItem);
            }
        }

        for (SalesReport consumedItem : consumedList) {
            SalesReport item = map.get(getReportKeyOfPeopleEffect(consumedItem));
            if (item != null) {
                item.consumedAmount = consumedItem.consumedAmount;
                item.consumedCost = consumedItem.consumedCost;
            } else {
                map.put(getReportKeyOfPeopleEffect(consumedItem), consumedItem);
            }

        }

        List resultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
            resultList.add(map.get(key));
        }
        condition.sort(resultList);
        return resultList;
    }


    public static List<SalesReport> queryNoContributionPeopleEffectData(SalesReportCondition condition, Boolean hasSeeReportProfitRight) {

        String sql = "select new models.SalesReport(o)" +
                " from Goods g,Supplier s,OperateUser o" +
                "  where g.supplierId =s.id and s.deleted=0 and s.salesId=o.id and g.isLottery=false ";

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(condition.jobNumber)) {
            sql = sql.concat("and o.jobNumber=:jobNumber");
            params.put("jobNumber", condition.jobNumber);
        }
        if (!hasSeeReportProfitRight) {
            sql = sql.concat("and o.id = :salesId");
            params.put("salesId", condition.salesId);
        }
        if (StringUtils.isNotBlank(condition.userName)) {
            sql = sql.concat("and o.userName like :userName");
            params.put("userName", "%" + condition.userName.trim() + "%");
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
        BigDecimal consumedCost = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal netCost = BigDecimal.ZERO;
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        Long totalBuyNumber = 0L;
        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            consumedAmount = consumedAmount.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            consumedCost = consumedCost.add(item.consumedCost == null ? BigDecimal.ZERO : item.consumedCost);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            totalBuyNumber = totalBuyNumber + (item.buyNumber == null ? 0l : item.buyNumber);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            netProfit = netProfit.add(item.profit == null ? BigDecimal.ZERO : item.profit).setScale(2, BigDecimal.ROUND_UP);
            netCost = netCost.add(item.netCost == null ? BigDecimal.ZERO : item.netCost).setScale(2, BigDecimal.ROUND_UP);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            totalSalePrice = totalSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
        }
        if (totalSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = netSalesAmount.subtract(netCost).divide(netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new SalesReport(totalAmount, refundAmount, consumedAmount, consumedCost, netProfit, grossMargin, totalBuyNumber, netSalesAmount, netCost);
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

        BigDecimal salesRefundAmount = BigDecimal.ZERO;
        BigDecimal previousSalesRefundAmount = BigDecimal.ZERO;
        BigDecimal consumedRefundAmount = BigDecimal.ZERO;
        BigDecimal previousConsumedRefundAmount = BigDecimal.ZERO;

        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal totalConsumed = BigDecimal.ZERO;
        BigDecimal cheatedOrderAmount = BigDecimal.ZERO;

        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal netCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;

        for (SalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            salesRefundAmount = salesRefundAmount.add(item.salesRefundAmount == null ? BigDecimal.ZERO : item.salesRefundAmount);
            previousSalesRefundAmount = previousSalesRefundAmount.add(item.previousSalesRefundAmount == null ? BigDecimal.ZERO : item.previousSalesRefundAmount);
            consumedRefundAmount = consumedRefundAmount.add(item.consumedRefundAmount == null ? BigDecimal.ZERO : item.consumedRefundAmount);
            previousConsumedRefundAmount = previousConsumedRefundAmount.add(item.previousConsumedRefundAmount == null ? BigDecimal.ZERO : item.previousConsumedRefundAmount);

            totalConsumed = totalConsumed.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            netCost = netCost.add(item.netCost == null ? BigDecimal.ZERO : item.netCost);

            cheatedOrderAmount = cheatedOrderAmount.add(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount);
        }
        if (netSalesAmount.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = netSalesAmount.subtract(netCost).divide(netSalesAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new SalesReport(totalConsumed.setScale(2, 4), totalAmount.setScale(2, 4), salesRefundAmount.setScale(2, 4), previousSalesRefundAmount.setScale(2, 4), consumedRefundAmount.setScale(2, 4), previousConsumedRefundAmount.setScale(2, 4), netSalesAmount.setScale(2, 4), grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4), cheatedOrderAmount.setScale(2, 4));
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
                return (arg.goods.code == null ? "" : arg.goods.code).compareTo(this.goods.code == null ? "" : this.goods.code);
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
