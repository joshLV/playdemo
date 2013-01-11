package models;

import models.accounts.AccountType;
import models.order.Order;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 渠道商品报表
 * <p/>
 * User: wangjia
 * Date: 12-12-25
 * Time: 上午10:16
 */
public class ChannelGoodsReport {
    public Order order;
    public String loginName;
    public String userName;
    public Goods goods;
    public BigDecimal avgSalesPrice;

    /**
     * 消费金额
     */
    public BigDecimal consumedAmount;

    /**
     * 刷单金额
     */
    public BigDecimal cheatedOrderAmount;

    /**
     * 刷单量
     */
    public Long cheatedOrderNum;

    /**
     * 消费金额汇总
     */
    public BigDecimal totalConsumed;

    /**
     * 刷单看陈本
     */
    public BigDecimal cheatedOrderCost;

    /**
     * 退款成本
     */
    public BigDecimal refundCost;

    /**
     * 从分销销来 的退款数量
     */
    public Long refundNum;

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
     * 毛利率
     */
    public BigDecimal grossMargin;
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

    /**
     * 渠道成本
     */
    public BigDecimal channelCost;

    public ChannelGoodsReport(Order order, Goods goods, BigDecimal originalPrice, Long buyNumber,
                              BigDecimal totalAmount, BigDecimal avgSalesPrice,
                              BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount
            , BigDecimal totalCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

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
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.goods = goods;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //refund ecoupon
    public ChannelGoodsReport(Order order, BigDecimal refundAmount, Goods goods, BigDecimal refundCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.refundAmount = refundAmount;
        this.goods = goods;
        this.refundCost = refundCost;
    }

    //cheated order
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.goods = goods;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
    }

    //consumedAt
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal consumedAmount) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.goods = goods;
        this.consumedAmount = consumedAmount;
    }

    //padiAt from resaler
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal totalAmountCommissionAmount, BigDecimal ratio, BigDecimal totalCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.ratio = ratio;
        this.goods = goods;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
        this.totalCost = totalCost;
    }

    //cheated order from resaler
    public ChannelGoodsReport(Order order, BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio, Goods goods, BigDecimal cheatedOrderCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.ratio = ratio;
        this.goods = goods;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
        this.cheatedOrderCost = cheatedOrderCost;
    }

    //refund from resaler
    public ChannelGoodsReport(Order order, BigDecimal refundCommissionAmount, Goods goods, BigDecimal ratio, Long refundNum, BigDecimal refundCost) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.ratio = ratio;
        this.goods = goods;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundNum = refundNum;
        this.refundCost = refundCost;
    }

    public ChannelGoodsReport(Order order, Long buyNumber, BigDecimal originalAmount) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public ChannelGoodsReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit, BigDecimal cheatedOrderAmount, BigDecimal totalConsumed) {
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.totalConsumed = totalConsumed;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> query(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                " )" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.order.userId, r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();


//        //from resaler
//        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
//                ",b.commissionRatio)" +
//                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
//        groupBy = " group by r.order.userId, r.goods.id";
//        query = JPA.em()
//                .createQuery(sql + condition.getResalerFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //paidAt from resaler
        sql = "select new models.ChannelGoodsReport(r.order,r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio" +
                " ,sum(r.originalPrice*r.buyNumber)) " +
                " from OrderItems r,Order o,Resaler b where ";
        groupBy = " group by r.order.userId,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.RESALER) + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

//        for (ChannelGoodsReport c : paidResalerResultList) {
//            System.out.println("c.loginName:" + c.loginName);
//            System.out.println("c.totalCost:" + c.totalCost);
//            System.out.println("");
//        }

        //cheated order
        sql = "select new models.ChannelGoodsReport(r.order,r.goods,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r and ";
        groupBy = " group by r.order.userId, r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder(AccountType.RESALER) + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelGoodsReport> cheatedOrderResultList = query.getResultList();

        //cheated order from resaler
        sql = "select new models.ChannelGoodsReport(r.order,sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100,b.commissionRatio,r.goods" +
                " ,sum(r.originalPrice))" +
                " from OrderItems r,Order o,Resaler b, ECoupon e where e.orderItems=r and";
        groupBy = " group by r.order.userId, r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResaler(AccountType.RESALER) + groupBy + " order by sum(r.buyNumber) desc ");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> cheatedOrderResalerResultList = query.getResultList();


        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e ";
        groupBy = " group by e.order.userId, e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();


        //refund from resaler
        sql = "select new models.ChannelGoodsReport(e.order,sum(e.refundPrice)*b.commissionRatio/100,r.goods,b.commissionRatio,sum(r)" +
                " ,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o";
        groupBy = " group by e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResaler(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundResalerResultList = query.getResultList();

//        for (ChannelGoodsReport r : refundResalerResultList) {
//            System.out.println("r.loginName:" + r.loginName);
//            System.out.println("r.refundCost:" + r.refundCost);
//            System.out.println("");
//        }

        //consumedAt
        sql = "select new models.ChannelGoodsReport(e.order,e.orderItems.goods,sum(r.salePrice-r.rebateValue/r.buyNumber)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        groupBy = " group by e.order.userId, e.orderItems.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelGoodsReport> consumedResultList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport cheatedItem : cheatedOrderResultList) {
            ChannelGoodsReport item = map.get(getReportKey(cheatedItem));
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

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.refundCost = refundItem.refundCost;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost);
            }
        }

        for (ChannelGoodsReport consumedItem : consumedResultList) {
            ChannelGoodsReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                Goods goods = Goods.findById(consumedItem.goods.id);
                consumedItem.originalPrice = goods.originalPrice;
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }

//        //merge from resaler if commissionRatio
//        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
//            ChannelGoodsReport item = map.get(getReportKey(resalerItem));
//            if (item == null) {
//                map.put(getReportKey(resalerItem), resalerItem);
//            } else {
//                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
//                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
//                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
//            }
//        }

        //merge from resaler if commissionRatio
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
//                System.out.println("item.profit:" + item.profit);
//                System.out.println("sales>>>>" + (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount));
//                System.out.println("item.totalAmount:" + item.totalAmount);
//                System.out.println("item.cheatedOrderAmount:" + item.cheatedOrderAmount);
//                System.out.println("item.refundAmount:" + item.refundAmount);
//                System.out.println("");

            }
        }

        totalCommission = BigDecimal.ZERO;
        for (ChannelGoodsReport cheatedResalerItem : cheatedOrderResalerResultList) {
            ChannelGoodsReport item = map.get(getReportKey(cheatedResalerItem));
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
        for (ChannelGoodsReport refundResalerItem : refundResalerResultList) {
            ChannelGoodsReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
//                System.out.println("sales>>>>" + (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount).subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost));
//                System.out.println("item.totalAmount:" + item.totalAmount);
//                System.out.println("item.cheatedOrderAmount:" + item.cheatedOrderAmount);
//                System.out.println("item.refundAmount:" + item.refundAmount);
//                System.out.println("item.totalCost:" + item.totalCost);
//                System.out.println("item.cheatedOrderAmount:" + item.cheatedOrderAmount);
//                System.out.println("item.refundCost:" + item.refundCost);
//                System.out.println("");
            }
        }

        List<ChannelGoodsReport> resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);

        }
        Collections.sort(tempString);

        for (String key : tempString) {
            resultList.add(map.get(key));
        }


        return resultList;
    }


    /**
     * 取得按商品统计的销售记录     consumer
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> queryConsumer(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                ")" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();

//        for (ChannelGoodsReport p : paidResultList) {
//            System.out.println("p.loginName:" + p.loginName);
//            System.out.println("p.totalCost:" + p.totalCost);
//            System.out.println("");
//        }

        //cheated order
        sql = "select new models.ChannelGoodsReport(r.order,r.goods,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r and ";
        groupBy = " group by r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelGoodsReport> cheatedOrderResultList = query.getResultList();

        //from resaler
        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
        groupBy = " group by  r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();

        //consumedAt
        sql = "select new models.ChannelGoodsReport(e.order,e.orderItems.goods,sum(r.salePrice-r.rebateValue/r.buyNumber)) " +
                " from OrderItems r, ECoupon e where e.orderItems=r";
        groupBy = " group by e.orderItems.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelGoodsReport> consumedResultList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            map.put(getConsumerReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport cheatedItem : cheatedOrderResultList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(cheatedItem));
            if (item == null) {
                Goods goods = Goods.findById(cheatedItem.goods.id);
                cheatedItem.originalPrice = goods.originalPrice;
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderCost == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderCost);
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
                map.put(getConsumerReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount.subtract(item.cheatedOrderAmount);
                item.profit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost);
            }
        }

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                map.put(getConsumerReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderCost).setScale(2);
                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        for (ChannelGoodsReport consumedItem : consumedResultList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(consumedItem));
            if (item == null) {
                Goods goods = Goods.findById(consumedItem.goods.id);
                consumedItem.originalPrice = goods.originalPrice;
                map.put(getConsumerReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }

        //merge from resaler if commissionRatio
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(resalerItem));
            if (item == null) {
                map.put(getConsumerReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.totalCost);
            }
        }


        List resultList = new ArrayList();
        for (String key : map.keySet()) {
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
    public static ChannelGoodsReport getNetSummary(List<ChannelGoodsReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ChannelGoodsReport(null, 0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal cheatedOrderAmount = BigDecimal.ZERO;
        BigDecimal totalConsumed = BigDecimal.ZERO;


        for (ChannelGoodsReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);

            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            cheatedOrderAmount = cheatedOrderAmount.add(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount);
            totalConsumed = totalConsumed.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        return new ChannelGoodsReport(totalAmount.setScale(2, 4), refundAmount.setScale(2, 4), netSalesAmount.setScale(2, 4), grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4), cheatedOrderAmount.setScale(2, 4), totalConsumed.setScale(2, 4));
    }

    public static String getReportKey(ChannelGoodsReport refoundItem) {
        if (refoundItem.order == null) {
            return String.valueOf(refoundItem.goods.id);
        } else {
            return String.valueOf(refoundItem.order.userId) + String.valueOf(refoundItem.goods.id);
        }
    }

    public static String getConsumerReportKey(ChannelGoodsReport refoundItem) {
        return String.valueOf(refoundItem.goods.id);
    }

}
