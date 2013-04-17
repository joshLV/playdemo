package models;


import models.accounts.AccountType;
import models.order.ECouponStatus;
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
public class OperateResaleSalesReport {
    public Order order;
    public String loginName;
    public String userName;


    /**
     * 售出券数
     */
    public long buyNumber = 0l;

    /**
     * 退款券数
     */
    public long refundNumber = 0l;

    /**
     * 售出实物数
     */
    public long realBuyNumber = 0l;

    /**
     * 退款实物数
     */
    public long realRefundNumber = 0l;


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
    public long consumedNumber = 0l;

    /**
     * 消费金额
     */
    public BigDecimal consumedPrice = BigDecimal.ZERO;
    /**
     * 虚拟验证券数
     */
    public long virtualVerifyNumber = 0l;

    /**
     * 虚拟验证金额
     */
    public BigDecimal virtualVerifyPrice = BigDecimal.ZERO;
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
     * paidAt ecoupon  resaler
     */
    public OperateResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
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
    public OperateResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
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
    public OperateResaleSalesReport(Order order, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
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
    }

    //sendAt real consumer
    public OperateResaleSalesReport(Order order, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
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


    public OperateResaleSalesReport(BigDecimal virtualVerifyPrice, Long virtualVerifyNumber, Order order, boolean virtualVerify) {
        this.order = order;
        if (order != null) {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
        }

        this.virtualVerifyNumber = virtualVerifyNumber;
        this.virtualVerifyPrice = virtualVerifyPrice;
    }

    public OperateResaleSalesReport(BigDecimal refundPrice, Long refundNumber, Order order) {
        this.order = order;
        if (order != null) {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
        }

        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
    }

    public OperateResaleSalesReport(BigDecimal consumedPrice, Order order, Long consumedNumber) {
        this.order = order;
        if (order != null) {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
        }

        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
    }

    public OperateResaleSalesReport(Long refundNumber, BigDecimal refundPrice, Order order) {
        this.order = order;
        if (order != null) {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
        }

        this.realRefundPrice = refundPrice;
        this.realRefundNumber = refundNumber;
    }


    public OperateResaleSalesReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.userName = "一百券";
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }

    public OperateResaleSalesReport(long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
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
    public OperateResaleSalesReport virtualVerifyPrice(BigDecimal virtualVerifyPrice) {
        this.virtualVerifyPrice= virtualVerifyPrice;
        return this;
    }
     public OperateResaleSalesReport virtualVerifyNumber(Long virtualVerifyNumber) {
        this.virtualVerifyNumber= virtualVerifyNumber;
        return this;
    }
    public OperateResaleSalesReport(long totalNumber, BigDecimal amount, long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
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
    public static List<OperateResaleSalesReport> query(
            OperateResaleSalesReportCondition condition) {

        //paidAt ecoupon
        String sql = "select new models.OperateResaleSalesReport(r.order, sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b,Supplier s where e.orderItems=r and r.order=o and o.userId=b.id" +
                " and r.goods.supplierId = s ";
        String groupBy = " group by r.order.userId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.OperateResaleSalesReport(r.order,sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.OperateResaleSalesReport(sum(r.salePrice-r.rebateValue/r.buyNumber),r.order,count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterOfECoupon(AccountType.RESALER, ECouponStatus.CONSUMED) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.OperateResaleSalesReport(sum(e.refundPrice),count(e),r.order) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterOfECoupon(AccountType.RESALER, ECouponStatus.REFUND) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> refundResultList = query.getResultList();

        //virtualVerify ecoupon
        sql = "select new models.OperateResaleSalesReport(sum(e.salePrice),count(e),e.order,e.virtualVerify) from ECoupon e ";
        groupBy = " group by e.order.userId";
        query = JPA.em()
                .createQuery(sql + condition.getFilterVirtualVerfiyAt(AccountType.RESALER) + groupBy + " order by sum(e.salePrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> virtualList = query.getResultList();

        //refundAt real need to do !!!!!
        Map<Long, OperateResaleSalesReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (OperateResaleSalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (OperateResaleSalesReport paidItem : sentRealResultList) {
            OperateResaleSalesReport item = map.get(getReportKey(paidItem));
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
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }


        //merge other 2
        for (OperateResaleSalesReport consumedItem : consumedResultList) {
            OperateResaleSalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (OperateResaleSalesReport refundItem : refundResultList) {
            OperateResaleSalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }
        for (OperateResaleSalesReport virtualItem : virtualList) {
            OperateResaleSalesReport item = map.get(getReportKey(virtualItem));
            if (item == null) {
                map.put(getReportKey(virtualItem), virtualItem);
            } else {
                item.virtualVerifyPrice = virtualItem.virtualVerifyPrice;
                item.virtualVerifyNumber = virtualItem.virtualVerifyNumber;
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

    public static OperateResaleSalesReport summary(List<OperateResaleSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new OperateResaleSalesReport(0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO
                    , BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long refundCount = 0l;
        long consumedCount = 0l;
        BigDecimal consumedPrice = BigDecimal.ZERO;
        long buyCount = 0l;
        long realBuyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal realAmount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        BigDecimal totRefundPrice = BigDecimal.ZERO;
        BigDecimal shouldGetPrice = BigDecimal.ZERO;
        BigDecimal haveGetPrice = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalVirtualPrice = BigDecimal.ZERO;
        long totalVirtualNumber = 0;

        for (OperateResaleSalesReport item : resultList) {

            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice);
            realBuyCount += item.realBuyNumber;
            realAmount = realAmount.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice);
            totRefundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
            refundPrice = refundPrice.add(totRefundPrice);
            refundCount += item.refundNumber;
            consumedCount += item.consumedNumber;
            totalVirtualNumber += item.virtualVerifyNumber;
            totalVirtualPrice = totalVirtualPrice.add(item.virtualVerifyPrice == null ? BigDecimal.ZERO : item.virtualVerifyPrice);
            if (item.consumedPrice != null) {
                consumedPrice = consumedPrice.add(item.consumedPrice);
            }
            shouldGetPrice = amount.subtract(refundPrice);
            haveGetPrice = BigDecimal.ZERO;
            totalSalePrice = totalSalePrice.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice));
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);

        }
        if (totalSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totalSalePrice.subtract(totalCost).divide(totalSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new OperateResaleSalesReport(buyCount, amount.setScale(2, 4), realBuyCount, realAmount.setScale(2, 4), refundPrice.setScale(2, 4), refundCount, consumedPrice.setScale(2, 4), consumedCount, shouldGetPrice.setScale(2, 4), haveGetPrice.setScale(2, 4)
                , grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4)).virtualVerifyNumber(totalVirtualNumber).virtualVerifyPrice(totalVirtualPrice.setScale(2, 4));
    }

    /**
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<OperateResaleSalesReport> queryConsumer(OperateResaleSalesReportCondition condition) {
        //paidAt ecoupon
        String sql = "select new models.OperateResaleSalesReport(min(r.order), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o where e.orderItems=r and r.order=o  ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.OperateResaleSalesReport(min(r.order),sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r where ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + " group by r.order.userId order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.OperateResaleSalesReport(sum(r.salePrice-r.rebateValue/r.buyNumber),min(r.order),count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterOfECoupon(AccountType.CONSUMER, ECouponStatus.CONSUMED) + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.OperateResaleSalesReport(sum(e.refundPrice),count(e),min(r.order)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterOfECoupon(AccountType.CONSUMER, ECouponStatus.REFUND) + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<OperateResaleSalesReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!
        OperateResaleSalesReport result = null;
        List<OperateResaleSalesReport> resultList = new ArrayList<>();
        if (paidResultList != null && paidResultList.size() > 0) {
            result = paidResultList.get(0);
            if (sentRealResultList != null && sentRealResultList.size() > 0 && sentRealResultList.get(0).realBuyNumber > 0) {
                result.realSalePrice = sentRealResultList.get(0).realSalePrice;
                result.realBuyNumber = sentRealResultList.get(0).realBuyNumber;
                BigDecimal totalSalesPrice = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(sentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : sentRealResultList.get(0).realSalePrice);
                BigDecimal totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);
                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    result.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                result.profit = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(sentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : sentRealResultList.get(0).realSalePrice)
                        .subtract(result.totalCost == null ? BigDecimal.ZERO : result.totalCost).subtract(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);

                result.totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);
//                result.channelCost = result.channelCost.add(sentRealResultList.get(0).channelCost);
//                result.profit = result.salePrice.add(sentRealResultList.get(0).realSalePrice)
//                        .subtract(result.channelCost)
//                        .subtract(result.totalCost.add(sentRealResultList.get(0).totalCost));


            }
            if (consumedResultList != null && consumedResultList.size() > 0) {
                result.consumedPrice = consumedResultList.get(0).consumedPrice;
                result.consumedNumber = consumedResultList.get(0).consumedNumber;
            }
            if (refundResultList != null && refundResultList.size() > 0) {
                result.refundNumber = refundResultList.get(0).refundNumber;
                result.refundPrice = refundResultList.get(0).refundPrice;
            }
            resultList.add(result);
        }


        return resultList;
    }

    private static Long getReportKey(OperateResaleSalesReport refoundItem) {
        return refoundItem.order.userId;
    }

}









