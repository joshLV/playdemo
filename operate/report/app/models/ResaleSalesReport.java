package models;


import models.order.Order;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
public class ResaleSalesReport {

    public Order order;
    public String loginName;
    public String userName;


    /**
     * 售出券数
     */
    public Long buyNumber = 0l;

    /**
     * 退款券数
     */
    public Long refundNumber = 0l;

    /**
     * 售出实物数
     */
    public Long realBuyNumber = 0l;

    /**
     * 退款实物数
     */
    public Long realRefundNumber = 0l;


    /**
     * 售出券金额
     */
    public BigDecimal salePrice = BigDecimal.ZERO;

    /**
     * 退款券金额
     */
    public BigDecimal refundPrice = BigDecimal.ZERO;

    /**
     * 售出实物金额
     */
    public BigDecimal realSalePrice = BigDecimal.ZERO;

    /**
     * 退款实物金额
     */
    public BigDecimal realRefundPrice = BigDecimal.ZERO;

    /**
     * 消费券数
     */
    public Long consumedNumber = 0l;

    /**
     * 消费金额
     */
    public BigDecimal consumedPrice = BigDecimal.ZERO;

    /**
     * 应收款金额
     */
    public BigDecimal shouldGetPrice;
    /**
     * 已收款金额
     */
    public BigDecimal haveGetPrice;

    public Long totalNumber;
    public Long realTotalNumber;
    public BigDecimal amount;
    public BigDecimal realAmount;
    public BigDecimal totalRefundPrice;

    /**
     * 总销售额
     */
    public BigDecimal totalAmount;

    /**
     * 总成本
     */
    public BigDecimal totalCost;

    /**
     * 毛利率
     */
    public BigDecimal grossMargin;

    /**
     * 渠道成本
     */
    public BigDecimal channelCost;

    /**
     * 净利润
     */
    public BigDecimal profit;

    /**
     * 贡献度
     */
    public BigDecimal contribution;

    /**
     * 退款的佣金
     */
    public BigDecimal refundCommissionAmount;

    /**
     * 实物销售的佣金
     */
    public BigDecimal realCommissionAmount;

    /**
     * 退款的成本
     */
    public BigDecimal refundCost;

    /**
     * 刷单金额
     */
    public BigDecimal cheatedAmount;
    /**
     * 刷单成本
     */
    public BigDecimal cheatedOrderCost;


    /**
     * 刷单佣金成本
     */
    public BigDecimal cheatedOrderCommissionAmount = BigDecimal.ZERO;


    /**
     * paidAt ecoupon  resaler
     */
    public ResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }
        if (salePrice != null) {
            this.salePrice = salePrice;
        } else {
            this.salePrice = BigDecimal.ZERO;
        }
        this.buyNumber = buyNumber;
        this.totalCost = totalCost;
        this.channelCost = channelCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }


    /**
     * paidAt ecoupon   consumer
     */
    public ResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }
        if (salePrice != null) {
            this.salePrice = salePrice;
        } else {
            this.salePrice = BigDecimal.ZERO;
        }
        this.buyNumber = buyNumber;
        this.totalCost = totalCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }


    //sendAt real   resaler
    public ResaleSalesReport(Order order, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit, BigDecimal realCommissionAmount) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.channelCost = channelCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.realCommissionAmount = realCommissionAmount;
    }

    //sendAt real consumer
    public ResaleSalesReport(Order order, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    //cheatedOrder
    public ResaleSalesReport(Order order, BigDecimal cheatedAmount, BigDecimal cheatedOrderCost, BigDecimal cheatedOrderCommissionAmount) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }
        this.cheatedAmount = cheatedAmount;
        this.cheatedOrderCost = cheatedOrderCost;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    public ResaleSalesReport(BigDecimal consumedPrice, Order order, Long consumedNumber) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }

        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
    }

    public ResaleSalesReport(BigDecimal refundPrice, Long refundNumber, Order order, BigDecimal refundCommissionAmount, BigDecimal refundCost) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }

        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundCost = refundCost;
    }

    public ResaleSalesReport(Long refundNumber, BigDecimal refundPrice, Order order) {
        this.order = order;
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }

        this.realRefundPrice = refundPrice;
        this.realRefundNumber = refundNumber;
    }


    public ResaleSalesReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.userName = "一百券";
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }

    public ResaleSalesReport(Long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
                             BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.totalRefundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.shouldGetPrice = shouldGetPrice;
        this.haveGetPrice = haveGetPrice;
    }

    public ResaleSalesReport(Long totalNumber, BigDecimal amount, Long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
                             BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.realTotalNumber = realTotalNumber;
        this.realAmount = realAmount;
        this.totalRefundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.shouldGetPrice = shouldGetPrice;
        this.haveGetPrice = haveGetPrice;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
    }


    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition) {

        //paidAt ecoupon
        String sql = "select new models.ResaleSalesReport(min(r.order), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b,Supplier s where e.orderItems=r and r.order=o and o.userId=b.id" +
                " and r.goods.supplierId = s ";
        String groupBy = " group by r.order.userId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ResaleSalesReport(r.order,sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum((r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber),sum((r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(r.salePrice-r.rebateValue/r.buyNumber),r.order,count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ResaleSalesReport(sum(e.salePrice),count(e),r.order,sum(e.salePrice*b.commissionRatio/100),sum(e.originalPrice)) " +
                "from OrderItems r, ECoupon e,Resaler b where e.orderItems=r and r.order.userId = b.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt() + groupBy + " order by sum(e.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ResaleSalesReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!

        //算出刷单金额和成本  cheatedOrder
        sql = "select new models.ResaleSalesReport(r.order," +
                "sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                " ,sum(r.originalPrice), sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100)" +
                ")" +
                " from OrderItems r,Order o,Resaler b, ECoupon e where e.orderItems=r and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy);


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ResaleSalesReport> cheatedOrderResultList = query.getResultList();


        Map<Long, ResaleSalesReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ResaleSalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ResaleSalesReport paidItem : sentRealResultList) {
            ResaleSalesReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                item.realSalePrice = paidItem.realSalePrice;
                item.realBuyNumber = paidItem.realBuyNumber;
                BigDecimal totalSalesPrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
                BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost.add(paidItem.channelCost);
//                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
                item.profit = item.profit.add(item.realSalePrice).subtract(paidItem.totalCost).subtract(paidItem.realCommissionAmount);
                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }


        //merge other 2
        for (ResaleSalesReport consumedItem : consumedResultList) {
            ResaleSalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (ResaleSalesReport refundItem : refundResultList) {
            ResaleSalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
                item.profit = item.profit.subtract(refundItem.refundPrice == null ? BigDecimal.ZERO : refundItem.refundPrice).add(refundItem.refundCost== null ? BigDecimal.ZERO : refundItem.refundCost).add(refundItem.refundCommissionAmount== null ? BigDecimal.ZERO : refundItem.refundCommissionAmount);
            }
        }

        for (ResaleSalesReport cheatedItem : cheatedOrderResultList) {
            ResaleSalesReport item = map.get(getReportKey(cheatedItem));
            if (item == null) {
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
                item.profit = item.profit.subtract(cheatedItem.cheatedAmount).add(cheatedItem.cheatedOrderCost);
            }
        }



        List resultList = new ArrayList();
        for (Long key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 消费者和分销商合计
     *
     * @param resultList
     * @return
     */

    public static ResaleSalesReport summary(List<ResaleSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ResaleSalesReport(0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO
                    , BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        Long refundCount = 0l;
        Long consumedCount = 0l;
        BigDecimal consumedPrice = BigDecimal.ZERO;
        Long buyCount = 0l;
        Long realBuyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal realAmount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        BigDecimal totRefundPrice = BigDecimal.ZERO;
        BigDecimal shouldGetPrice = BigDecimal.ZERO;
        BigDecimal haveGetPrice = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (ResaleSalesReport item : resultList) {

            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice);
            realBuyCount += item.realBuyNumber;
            realAmount = realAmount.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice);
            totRefundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
            refundPrice = refundPrice.add(totRefundPrice);
            refundCount += item.refundNumber;
            consumedCount += item.consumedNumber;

            if (item.consumedPrice != null) {
                consumedPrice = consumedPrice.add(item.consumedPrice);
            }
            shouldGetPrice = amount.subtract(refundPrice);
            haveGetPrice = BigDecimal.ZERO;
            totolSalePrice = totolSalePrice.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice));
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
        }
        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new ResaleSalesReport(buyCount, amount.setScale(2, 4), realBuyCount, realAmount.setScale(2, 4), refundPrice.setScale(2, 4), refundCount, consumedPrice.setScale(2, 4), consumedCount, shouldGetPrice.setScale(2, 4), haveGetPrice.setScale(2, 4)
                , grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4));
    }

    private static Long getReportKey(ResaleSalesReport refoundItem) {
        return refoundItem.order.userId;
    }

}









