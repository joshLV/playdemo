package models;

import models.accounts.AccountType;
import models.order.Order;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 客流报表
 * <p/>
 * User: wangjia
 * Date: 12-12-28
 * Time: 下午5:30
 */
public class ConsumerFlowReport implements Comparable<ConsumerFlowReport> {
    public Order order;

    public Long orderNum;
    public String loginName;
    public String userName;
    public String orderBy;
    /**
     * 发生日期.
     */
    public String date;


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

    public BigDecimal refundAmount = BigDecimal.ZERO;

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
     * 客单价
     */
    public BigDecimal perOrderPrice;
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
     * paidAt ecoupon resaler
     */
    public ConsumerFlowReport(String date, Long orderNum, BigDecimal perOrderPrice, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
        this.perOrderPrice = perOrderPrice;
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
     * paidAt ecoupon   consumer (order not necessary but for not tuple)
     */
    public ConsumerFlowReport(Order order, BigDecimal salePrice, Long orderNum, BigDecimal perOrderPrice, String date, Long buyNumber, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
        this.perOrderPrice = perOrderPrice;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
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


    //sendAt real resaler
    public ConsumerFlowReport(String date, Long orderNum, BigDecimal perOrderPrice, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
        this.perOrderPrice = perOrderPrice;
        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.channelCost = channelCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    //sendAt real consumer
    public ConsumerFlowReport(Order order, String date, Long orderNum, BigDecimal perOrderPrice, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
        this.perOrderPrice = perOrderPrice;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    public ConsumerFlowReport(String date, BigDecimal consumedPrice, Long orderNum, Long consumedNumber) {
        this.date = date;
        this.orderNum = orderNum;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
    }

    public ConsumerFlowReport(Long orderNum, String date, BigDecimal refundPrice, Long refundNumber) {
        this.date = date;
        this.orderNum = orderNum;
        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
    }

    public ConsumerFlowReport(String date, Long refundNumber, BigDecimal refundPrice, Long orderNum) {
        this.date = date;
        this.orderNum = orderNum;
        this.realRefundPrice = refundPrice;
        this.realRefundNumber = refundNumber;
    }


    public ConsumerFlowReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }

    public ConsumerFlowReport(Long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
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

    public ConsumerFlowReport(Long totalNumber, BigDecimal amount, Long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
                              BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit, BigDecimal perOrderPrice) {
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
        this.perOrderPrice = perOrderPrice;
    }

    /**
     * 客流报表统计
     *
     * @param condition
     * @return
     */
    public static List<ConsumerFlowReport> query(
            ConsumerFlowReportCondition condition, String orderBy) {

        //paidAt ecoupon  resaler
        String sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id)" +
                " ,sum(r.salePrice-r.rebateValue/r.buyNumber)/count(r.order.id), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio)/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio)/100-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b,Supplier s where e.orderItems=r and r.order=o and o.userId=b.id " +
                " and r.goods.supplierId = s ";
        String groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)) ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> paidResalerResultList = query.getResultList();


        //paidAt ecoupon  consumer  (order not necessary but for not tuple)
        sql = "select new models.ConsumerFlowReport(r.order,sum(r.salePrice-r.rebateValue/r.buyNumber), count(r.order.id)," +
                " sum(r.salePrice-r.rebateValue/r.buyNumber)/count(r.order.id),str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o where e.orderItems=r and r.order=o  ";

        query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> paidConsumerResultList = query.getResultList();


        //sendAt real resaler
        sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id)" +
                " ,sum(r.salePrice*r.buyNumber-r.rebateValue)/count(r.order.id),sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum((r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio)/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum((r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio)/100-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> sentResalerRealResultList = query.getResultList();

        //sendAt real consumer  (order not necessary)
        sql = "select new models.ConsumerFlowReport(r.order,str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id)," +
                " sum(r.salePrice*r.buyNumber-r.rebateValue)/count(r.order.id),sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r where ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> sentConsumerRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.ConsumerFlowReport(str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)),sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.order.id),count(e)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        groupBy = " group by str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)) ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by e.consumedAt desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<ConsumerFlowReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ConsumerFlowReport(sum(r.order.id),str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt)),sum(e.refundPrice),count(e))" +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        groupBy = " group by str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt))  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt() + groupBy + " order by e.refundAt desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<ConsumerFlowReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!


        Map<String, ConsumerFlowReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ConsumerFlowReport paidItem : paidResalerResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ConsumerFlowReport paidItem : paidConsumerResultList) {
            ConsumerFlowReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                item.salePrice = item.salePrice.add(paidItem.salePrice);
                item.buyNumber = item.buyNumber + paidItem.buyNumber;
                item.realSalePrice = item.realSalePrice.add(paidItem.realSalePrice);
                item.realBuyNumber = item.realBuyNumber + paidItem.realBuyNumber;
                item.refundPrice = item.refundPrice.add(paidItem.refundPrice);
                item.refundNumber = item.refundNumber + paidItem.refundNumber;
                item.consumedPrice = item.consumedPrice.add(paidItem.consumedPrice);
                item.consumedNumber = item.consumedNumber + paidItem.consumedNumber;
                item.totalCost = item.totalCost.add(paidItem.totalCost);
                item.orderNum = item.orderNum + paidItem.orderNum;
                item.perOrderPrice = item.salePrice.divide(BigDecimal.valueOf(item.orderNum), 2, RoundingMode.HALF_UP);

                item.grossMargin = item.salePrice.subtract(item.totalCost).divide(item.salePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                item.channelCost = item.channelCost.add(paidItem.channelCost == null ? BigDecimal.ZERO : paidItem.channelCost);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.channelCost);
            }
        }

        for (ConsumerFlowReport paidItem : sentResalerRealResultList) {
            ConsumerFlowReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                item.realSalePrice = paidItem.realSalePrice;
                item.realBuyNumber = paidItem.realBuyNumber;
                BigDecimal totalSalesPrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
                BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                BigDecimal totalSalePrice = item.salePrice.add(paidItem.realSalePrice);
                item.orderNum = item.orderNum + paidItem.orderNum;
                item.perOrderPrice = totalSalePrice.divide(BigDecimal.valueOf(item.orderNum), 2, RoundingMode.HALF_UP);

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost.add(paidItem.channelCost);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }

        for (ConsumerFlowReport paidItem : sentConsumerRealResultList) {
            ConsumerFlowReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                item.realSalePrice = item.realSalePrice.add(paidItem.realSalePrice);
                item.realBuyNumber = item.realBuyNumber + paidItem.realBuyNumber;
                BigDecimal totalSalesPrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
                BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                BigDecimal totalSalePrice = item.salePrice.add(paidItem.realSalePrice).add(item.realSalePrice);
                item.orderNum = item.orderNum + paidItem.orderNum;
                item.perOrderPrice = totalSalePrice.divide(BigDecimal.valueOf(item.orderNum), 2, RoundingMode.HALF_UP);

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost.add(paidItem.channelCost == null ? BigDecimal.ZERO : paidItem.channelCost);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost).subtract(item.channelCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }

        //merge other 2
        for (ConsumerFlowReport consumedItem : consumedResultList) {
            ConsumerFlowReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (ConsumerFlowReport refundItem : refundResultList) {
            ConsumerFlowReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }

        List<ConsumerFlowReport> resultList = new ArrayList();
        for (String key : map.keySet()) {
            map.get(key).orderBy = orderBy;
            resultList.add(map.get(key));
        }

        return resultList;
    }

    public static ConsumerFlowReport summary(List<ConsumerFlowReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ConsumerFlowReport(0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO
                    , BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
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
        Long orderNum = 0l;
        BigDecimal perOrderPrice = BigDecimal.ZERO;

        for (ConsumerFlowReport item : resultList) {

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
            orderNum = orderNum + item.orderNum;

        }
        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            perOrderPrice = totolSalePrice.divide(BigDecimal.valueOf(orderNum), 2, RoundingMode.HALF_UP);
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new ConsumerFlowReport(buyCount, amount.setScale(2, 4), realBuyCount, realAmount.setScale(2, 4), refundPrice.setScale(2, 4), refundCount, consumedPrice.setScale(2), consumedCount, shouldGetPrice.setScale(2, 4), haveGetPrice.setScale(2, 4)
                , grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4), perOrderPrice.setScale(2, 4));
    }

    private static String getReportKey(ConsumerFlowReport refoundItem) {
        return refoundItem.date;
    }

    public Date getOrder() {
        return com.uhuila.common.util.DateUtil.stringToDate(date, "yyyy-MM-dd");
    }

    //
//    @Override
//    public int compareTo(ConsumerFlowReport arg) {
//        return arg.getOrder().compareTo(this.getOrder());
//    }
    @Override
    public int compareTo(ConsumerFlowReport arg) {
//         后面一位：1是升序，2是降序
        switch (this.orderBy) {
            case "02":
                return arg.getOrder().compareTo(this.getOrder());
            case "01":
                return this.getOrder().compareTo(arg.getOrder());
            case "12":
                return (arg.salePrice == null ? BigDecimal.ZERO : arg.salePrice).compareTo(this.salePrice == null ? BigDecimal.ZERO : this.salePrice);
            case "11":
                return (this.salePrice == null ? BigDecimal.ZERO : this.salePrice).compareTo(arg.salePrice == null ? BigDecimal.ZERO : arg.salePrice);
            case "22":
                return (arg.realAmount == null ? BigDecimal.ZERO : arg.realAmount).compareTo(this.realAmount == null ? BigDecimal.ZERO : this.realAmount);
            case "21":
                return (this.realAmount == null ? BigDecimal.ZERO : this.realAmount).compareTo(arg.realAmount == null ? BigDecimal.ZERO : arg.realAmount);
            case "32":
                return (arg.refundPrice == null ? BigDecimal.ZERO : arg.refundPrice).compareTo(this.refundPrice == null ? BigDecimal.ZERO : this.refundPrice);
            case "31":
                return (this.refundPrice == null ? BigDecimal.ZERO : this.refundPrice).compareTo(arg.refundPrice == null ? BigDecimal.ZERO : arg.refundPrice);
            case "42":
                return (arg.consumedPrice == null ? BigDecimal.ZERO : arg.consumedPrice).compareTo(this.consumedPrice == null ? BigDecimal.ZERO : this.consumedPrice);
            case "41":
                return (this.consumedPrice == null ? BigDecimal.ZERO : this.consumedPrice).compareTo(arg.consumedPrice == null ? BigDecimal.ZERO : arg.consumedPrice);
            case "52":
                return (arg.perOrderPrice == null ? BigDecimal.ZERO : arg.perOrderPrice).compareTo(this.perOrderPrice == null ? BigDecimal.ZERO : this.perOrderPrice);
            case "51":
                return (this.perOrderPrice == null ? BigDecimal.ZERO : this.perOrderPrice).compareTo(arg.perOrderPrice == null ? BigDecimal.ZERO : arg.perOrderPrice);
            case "62":
                return (arg.grossMargin == null ? BigDecimal.ZERO : arg.grossMargin).compareTo(this.grossMargin == null ? BigDecimal.ZERO : this.grossMargin);
            case "61":
                return (this.grossMargin == null ? BigDecimal.ZERO : this.grossMargin).compareTo(arg.grossMargin == null ? BigDecimal.ZERO : arg.grossMargin);
            case "72":
                return (arg.channelCost == null ? BigDecimal.ZERO : arg.channelCost).compareTo(this.channelCost == null ? BigDecimal.ZERO : this.channelCost);
            case "71":
                return (this.channelCost == null ? BigDecimal.ZERO : this.channelCost).compareTo(arg.channelCost == null ? BigDecimal.ZERO : arg.channelCost);
            case "82":
                return (arg.profit == null ? BigDecimal.ZERO : arg.profit).compareTo(this.profit == null ? BigDecimal.ZERO : this.profit);
            case "81":
                return (this.profit == null ? BigDecimal.ZERO : this.profit).compareTo(arg.profit == null ? BigDecimal.ZERO : arg.profit);
        }
        return 0;
    }
}
