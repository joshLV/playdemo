package models;

import models.order.Order;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 总成本(coupon+real)
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

    /*
        退款成本
     */
    public BigDecimal refundCost;

    /**
     * 退款佣金成本
     */
    public BigDecimal refundCommissionAmount = BigDecimal.ZERO;

    /**
     * 净利润
     */
    public BigDecimal profit;

    /**
     * 刷单金额
     */
    public BigDecimal cheatedOrderAmount;

    /**
     * 刷单成本
     */
    public BigDecimal cheatedOrderCost;

    /**
     * 刷单佣金成本
     */
    public BigDecimal cheatedOrderCommissionAmount = BigDecimal.ZERO;


    /**
     * 销售佣金成本
     */
    public BigDecimal salesCommissionAmount = BigDecimal.ZERO;


    /**
     * 实物销售佣金成本
     */
    public BigDecimal salesRealCommissionAmount = BigDecimal.ZERO;

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
     * paidAt ecoupon    (order not necessary but for not tuple)
     */
    public ConsumerFlowReport(Order order, BigDecimal salePrice, Long orderNum, BigDecimal perOrderPrice, String date, Long buyNumber, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit,Order o) {
        this.date = date;
        this.orderNum = orderNum;
        this.perOrderPrice = perOrderPrice;
        if (order != null) {
//            if (order.userType == AccountType.CONSUMER) {
//                this.loginName = "一百券";
//            } else {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
//            }
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
        this.order=o;
    }

    //paidat resaler
    public ConsumerFlowReport(BigDecimal salesCommissionAmount, String date) {
        this.date = date;
        this.salesCommissionAmount = salesCommissionAmount;
        System.out.println(salesCommissionAmount + "《=========salesCommissionAmount:");
        System.out.println(  "padi《=========:");
    }

    //sendAt resaler
    public ConsumerFlowReport(BigDecimal salesRealCommissionAmount, String date, Long num) {
        this.date = date;
        this.salesRealCommissionAmount = salesRealCommissionAmount;
        System.out.println(salesRealCommissionAmount + "《=========salesRealCommissionAmount:");
        System.out.println( "sendat《=========:");
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
//            if (order.userType == AccountType.CONSUMER) {
//                this.loginName = "一百券";
//            } else {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
//            }
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

    //refundAt
    public ConsumerFlowReport(Long orderNum, String date, BigDecimal refundPrice, Long refundNumber, BigDecimal refundCost) {
        this.date = date;
        this.orderNum = orderNum;
        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
        this.refundCost = refundCost;
    }

    //refundAt resaler
    public ConsumerFlowReport(String date, BigDecimal refundCommissionAmount) {
        this.date = date;
        this.refundCommissionAmount = refundCommissionAmount;
    }


    //cheatedAt
    public ConsumerFlowReport(String date, BigDecimal cheatedOrderAmount, BigDecimal cheatedOrderCost) {
        this.date = date;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderCost = cheatedOrderCost;

    }

    //cheatedAt
    public ConsumerFlowReport(Long num, String date, BigDecimal cheatedOrderCommissionAmount) {
        this.date = date;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
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

        //算出售出券金额  paidAt ecoupon
        String sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id)" +
                " ,sum(r.salePrice-r.rebateValue/r.buyNumber)/count(r.order.id), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio)/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b,Supplier s where e.orderItems=r and r.order=o and o.userId=b.id " +
                " and r.goods.supplierId = s ";
        String groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)) ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> paidResultList = query.getResultList();


        //算出售出券金额 佣金  paidAt ecoupon resaler
        sql = "select new models.ConsumerFlowReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))" +
                " )" +
                " from OrderItems r, Order o,Resaler b where r.goods.materialType=models.sales.MaterialType.ELECTRONIC and r.order=o and o.userId=b.id " +
                " ";
        groupBy = " group by b,str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)) ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> paidResalerResultList = query.getResultList();


        //计算实物售出金额  sendAt real
        sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id)" +
                " ,sum(r.salePrice*r.buyNumber-r.rebateValue)/count(r.order.id),sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum((r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio)/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> sentRealResultList = query.getResultList();

        //计算实物售出金额 佣金 sendAt real
        sql = "select new models.ConsumerFlowReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))" +
                " ,count(*)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        groupBy = " group by b,str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> sentRealResalerResultList = query.getResultList();


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

        //计算退款  refundAt ecoupon
        sql = "select new models.ConsumerFlowReport(sum(r.order.id),str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt)),sum(e.refundPrice),count(e)," +
                "sum(e.orderItems.originalPrice))" +
                " from OrderItems r, Order o, ECoupon e where e.orderItems=r  ";
        groupBy = " group by str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt)) ";
//
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt() + groupBy);
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<ConsumerFlowReport> refundResultList = query.getResultList();


        //计算退款的佣金  refundAt resaler
        sql = "select new models.ConsumerFlowReport(str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt))" +
                ",sum(e.refundPrice)*b.commissionRatio/100 )" +
                " from OrderItems r, Order o, ECoupon e,Resaler b where e.orderItems=r  and o.userId=b.id  ";
        groupBy = " group by b,str(year(e.refundAt))||'-'||str(month(e.refundAt))||'-'||str(day(e.refundAt)) ";
//
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt() + groupBy);
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<ConsumerFlowReport> refundResalerResultList = query.getResultList();


        //refundAt real need to do !!!!!

        //算出刷单金额和成本  cheatedOrder
        sql = "select new models.ConsumerFlowReport(str(year(e.order.paidAt))||'-'||str(month(e.order.paidAt))||'-'||str(day(e.order.paidAt))," +
                "sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                " ,sum(r.originalPrice))" +
                " from OrderItems r,Order o,Resaler b, ECoupon e where e.orderItems=r and ";
        groupBy = " group by str(year(e.order.paidAt))||'-'||str(month(e.order.paidAt))||'-'||str(day(e.order.paidAt)) ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy);


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ConsumerFlowReport> cheatedOrderResultList = query.getResultList();


        //算出刷单 佣金  cheatedOrder   resaler
        sql = "select new models.ConsumerFlowReport(count(*),str(year(e.order.paidAt))||'-'||str(month(e.order.paidAt))||'-'||str(day(e.order.paidAt))," +
                " sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100 )" +
                " from OrderItems r,Order o,Resaler b, ECoupon e where e.orderItems=r and ";
        groupBy = " group by b,str(year(e.order.paidAt))||'-'||str(month(e.order.paidAt))||'-'||str(day(e.order.paidAt)) ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy);


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ConsumerFlowReport> cheatedOrderResalerResultList = query.getResultList();


        Map<String, ConsumerFlowReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ConsumerFlowReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }


        for (ConsumerFlowReport paidItem : sentRealResultList) {
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

                item.profit = item.profit.add(paidItem.realSalePrice).subtract(paidItem.totalCost);
//                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
//                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }

        for (ConsumerFlowReport paidResalerItem : paidResalerResultList) {
            ConsumerFlowReport item = map.get(getReportKey(paidResalerItem));
            if (item == null) {
                map.put(getReportKey(paidResalerItem), paidResalerItem);
            } else {
                System.out.println(paidResalerItem.salesCommissionAmount + "《=========paidResalerItem.salesCommissionAmount:");
//                System.out.println(paidResalerItem.order.id + "《=========paidRealResalerItem.order.userType:");

                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit)
                        .subtract(paidResalerItem.salesCommissionAmount == null ? BigDecimal.ZERO : paidResalerItem.salesCommissionAmount);
            }
        }



        for (ConsumerFlowReport paidRealResalerItem : sentRealResultList) {
            ConsumerFlowReport item = map.get(getReportKey(paidRealResalerItem));
            if (item == null) {
                map.put(getReportKey(paidRealResalerItem), paidRealResalerItem);
            } else {
                System.out.println(paidRealResalerItem.salesRealCommissionAmount + "《=========paidRealResalerItem.salesRealCommissionAmount:");
//                System.out.println(paidRealResalerItem.order.userType + "《=========paidRealResalerItem.order.userType:");
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit)
                        .subtract(paidRealResalerItem.salesRealCommissionAmount == null ? BigDecimal.ZERO : paidRealResalerItem.salesRealCommissionAmount)  ;

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
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost).add(refundItem.refundCommissionAmount);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
                item.refundCost = refundItem.refundCost;
                item.profit = item.profit.subtract(item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice)
                        .add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost);
                item.refundCommissionAmount = refundItem.refundCommissionAmount;

            }
        }

        for (ConsumerFlowReport refundResalerItem : refundResalerResultList) {
            ConsumerFlowReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit)
                        .add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
            }
        }

        for (ConsumerFlowReport cheatedItem : cheatedOrderResultList) {
            ConsumerFlowReport item = map.get(getReportKey(cheatedItem));
            if (item == null) {
                item.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).add(cheatedItem.cheatedOrderCost);
                map.put(getReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.profit = item.profit.subtract(item.cheatedOrderAmount).add(item.cheatedOrderCost);
            }
        }

        for (ConsumerFlowReport cheatedOrderResalerItem : cheatedOrderResalerResultList) {
            ConsumerFlowReport item = map.get(getReportKey(cheatedOrderResalerItem));
            if (item == null) {
                map.put(getReportKey(cheatedOrderResalerItem), cheatedOrderResalerItem);
            } else {
                item.profit = (item.profit == null ? BigDecimal.ZERO : item.profit) ;
//                        .add(cheatedOrderResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedOrderResalerItem.cheatedOrderCommissionAmount);
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
