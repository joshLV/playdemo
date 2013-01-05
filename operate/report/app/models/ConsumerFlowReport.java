package models;

import models.accounts.AccountType;
import models.order.Order;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-28
 * Time: 下午5:30
 * To change this template use File | Settings | File Templates.
 */
public class ConsumerFlowReport implements Comparable<ConsumerFlowReport> {
    public Order order;

    public Long orderNum;
    public String loginName;
    public String userName;

    /**
     * 发生日期.
     */
    public String date;


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
     * paidAt ecoupon resaler
     */
    public ConsumerFlowReport(String date, Long orderNum, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
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
    public ConsumerFlowReport(Order order, BigDecimal salePrice, Long orderNum, String date, Long buyNumber, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
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
    public ConsumerFlowReport(String date, Long orderNum, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.channelCost = channelCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    //sendAt real consumer
    public ConsumerFlowReport(Order order, String date, Long orderNum, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.date = date;
        this.orderNum = orderNum;
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

    public ConsumerFlowReport(long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
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

    public ConsumerFlowReport(long totalNumber, BigDecimal amount, long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
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
     * 客流报表统计
     *
     * @param condition
     * @return
     */
    public static List<ConsumerFlowReport> query(
            ConsumerFlowReportCondition condition) {

        //paidAt ecoupon  resaler
        String sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.order.id), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
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
        sql = "select new models.ConsumerFlowReport(r.order,sum(r.salePrice-r.rebateValue/r.buyNumber), count(r.order.id),str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),count(r.buyNumber)" +
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
        sql = "select new models.ConsumerFlowReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),sum(r.order.id),count(r.buyNumber),sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                ",sum(r.originalPrice),sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio)/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum((r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio)/100-sum(r.originalPrice)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt))  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ConsumerFlowReport> sentResalerRealResultList = query.getResultList();

        //sendAt real consumer  (order not necessary)
        sql = "select new models.ConsumerFlowReport(r.order,str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),sum(r.order.id),count(r.buyNumber),sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r where ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
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

                item.grossMargin = item.salePrice.subtract(item.totalCost).divide(item.salePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                item.channelCost = item.channelCost.add(paidItem.channelCost == null ? BigDecimal.ZERO : paidItem.channelCost);
                System.out.println("item.protit>>>>" + item.profit);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(item.channelCost);
                System.out.println("item.protit2>>>>" + item.profit);
                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
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

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost.add(paidItem.channelCost);
                System.out.println("sent resale  profit>>>" + item.profit);
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

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost.add(paidItem.channelCost == null ? BigDecimal.ZERO : paidItem.channelCost);
                System.out.println("sent  consumer profit>>>" + item.profit);
                System.out.println("paidItem.realSalePrice>>>>" + paidItem.realSalePrice);
                System.out.println(" item.salePrice>>>>" + item.salePrice);
                System.out.println("item.totalcost>>>" + item.totalCost);
                System.out.println("paidItem.totalcost>>>" + paidItem.totalCost);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost).subtract(item.channelCost);
                System.out.println("sent 222 consumer profit>>>" + item.profit);

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
            resultList.add(map.get(key));
        }

//        for (ConsumerFlowReport c : resultList) {
//            System.out.println("profit>>>>" + c.profit);
//        }

        return resultList;
    }


    private static String getReportKey(ConsumerFlowReport refoundItem) {
        return refoundItem.date;
    }

    public Date getOrder() {
        return com.uhuila.common.util.DateUtil.stringToDate(date, "yyyy-MM-dd");
    }

    @Override
    public int compareTo(ConsumerFlowReport arg) {
        return arg.getOrder().compareTo(this.getOrder());
    }
}
