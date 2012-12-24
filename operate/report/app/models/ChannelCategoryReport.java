package models;

import models.accounts.AccountType;
import models.order.Order;
import models.order.OrderItems;
import models.supplier.SupplierCategory;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道大类报表
 * <p/>
 * User: wangjia
 * Date: 12-12-21
 * Time: 上午9:55
 */
public class ChannelCategoryReport {
    public Order order;
    public OrderItems orderItems;

    /**
     * 帐号
     */
    public String loginName;
    public String userName;


    public Long supplierCategorySize;

    /**
     * 商户类别
     */
    public String code;
    public String name;

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
     * paidAt ecoupon  resaler
     */
    public ChannelCategoryReport(Order order, Long supplierCategoryId,
                                 BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
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
    public ChannelCategoryReport(Order order, Long supplierCategoryId, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
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
    public ChannelCategoryReport(Order order, Long supplierCategoryId, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal channelCost, BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.channelCost = channelCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    //sendAt real consumer
    public ChannelCategoryReport(Order order, Long supplierCategoryId, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        this.realSalePrice = salePrice;
        this.realBuyNumber = buyNumber;
        this.totalCost = totalCost;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }


    public ChannelCategoryReport(BigDecimal consumedPrice, Long supplierCategoryId, Order order, Long consumedNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
    }

    public ChannelCategoryReport(BigDecimal refundPrice, Long supplierCategoryId, Long refundNumber, Order order) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        this.refundPrice = refundPrice;
        this.refundNumber = refundNumber;
    }


    public ChannelCategoryReport(Long refundNumber, BigDecimal refundPrice, Order order) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.realRefundPrice = refundPrice;
        this.realRefundNumber = refundNumber;
    }


    public ChannelCategoryReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.userName = "一百券";
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }


    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelCategoryReport> query(
            ChannelCategoryReportCondition condition) {

        //paidAt ecoupon
        String sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id" +
                ", sum(r.salePrice-r.rebateValue),count(r.buyNumber)" +
                ",sum(r.goods.originalPrice),sum(r.salePrice-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue)-sum(r.salePrice-r.rebateValue)*b.commissionRatio/100-sum(r.goods.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b, Supplier s where e.orderItems=r and r.order=o and o.userId=b.id " +
                " and r.goods.supplierId = s ";
        String groupBy = " group by r.order.userId, s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by r.order.userId,sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.ChannelCategoryReport(r.order,s.supplierCategory.id,count(r.buyNumber) " +
                ",sum(r.salePrice-r.rebateValue)" +
                ",sum(r.goods.originalPrice),sum(r.salePrice-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue)-sum(r.salePrice-r.rebateValue)*b.commissionRatio/100-sum(r.goods.originalPrice)" +
                ") from OrderItems r,Order o,Resaler b,Supplier s  where r.order=o and o.userId=b.id  and r.goods.supplierId = s and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();

        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue),s.supplierCategory.id,r.order,count(e))" +
                " from OrderItems r, ECoupon e,Supplier s  where e.orderItems=r and r.goods.supplierId = s ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();


        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue),s.supplierCategory.id, count(e),r.order) " +
                " from OrderItems r, ECoupon e ,Supplier s where e.orderItems=r and r.goods.supplierId = s ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> refundResultList = query.getResultList();


        //refundAt real need to do !!!!!

        Map<String, ChannelCategoryReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ChannelCategoryReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ChannelCategoryReport paidItem : sentRealResultList) {
            ChannelCategoryReport item = map.get(getReportKey(paidItem));
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
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }


        //merge other 2
        for (ChannelCategoryReport consumedItem : consumedResultList) {
            ChannelCategoryReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (ChannelCategoryReport refundItem : refundResultList) {
            ChannelCategoryReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }
        List resultList = new ArrayList();
        for (String key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelCategoryReport> queryConsumer(ChannelCategoryReportCondition condition) {
        //paidAt ecoupon
        String sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id, sum(r.salePrice-r.rebateValue),count(r.buyNumber)" +
                ",sum(r.goods.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice)" +
                ") from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s  ";
        String groupBy = " group by s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id, count(r.buyNumber),sum(r.salePrice-r.rebateValue)" +
                ",sum(r.goods.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice-r.rebateValue)-sum(r.goods.originalPrice)" +
                ") from OrderItems r , Supplier s where r.goods.supplierId = s and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();

        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue), s.supplierCategory.id, r.order,count(e)) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue), s.supplierCategory.id, count(e),r.order) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> refundResultList = query.getResultList();
        //refundAt real need to do !!!!!


//        ChannelCategoryReport result = null;
//        List<ChannelCategoryReport> resultList = new ArrayList<>();
//        if (paidResultList != null && paidResultList.size() > 0) {
//            result = paidResultList.get(0);
//            if (sentRealResultList != null && sentRealResultList.size() > 0 && sentRealResultList.get(0).realBuyNumber > 0) {
//                result.realSalePrice = sentRealResultList.get(0).realSalePrice;
//                result.realBuyNumber = sentRealResultList.get(0).realBuyNumber;
//                BigDecimal totalSalesPrice = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(sentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : sentRealResultList.get(0).realSalePrice);
//                BigDecimal totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);
//                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
//                    result.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
//                }
//
//                result.profit = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(sentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : sentRealResultList.get(0).realSalePrice)
//                        .subtract(result.totalCost == null ? BigDecimal.ZERO : result.totalCost).subtract(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);
//                result.totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(sentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : sentRealResultList.get(0).totalCost);
////                result.channelCost = result.channelCost.add(sentRealResultList.get(0).channelCost);
////                result.profit = result.salePrice.add(sentRealResultList.get(0).realSalePrice)
////                        .subtract(result.channelCost)
////                        .subtract(result.totalCost.add(sentRealResultList.get(0).totalCost));
//            }
//            if (consumedResultList != null && consumedResultList.size() > 0) {
//                result.consumedPrice = consumedResultList.get(0).consumedPrice;
//                result.consumedNumber = consumedResultList.get(0).consumedNumber;
//            }
//            if (refundResultList != null && refundResultList.size() > 0) {
//                result.refundNumber = refundResultList.get(0).refundNumber;
//                result.refundPrice = refundResultList.get(0).refundPrice;
//            }
//            resultList.add(result);


        //refundAt real need to do !!!!!

        Map<String, ChannelCategoryReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (ChannelCategoryReport paidItem : paidResultList) {
            map.put(getConsumerReportKey(paidItem), paidItem);
        }

        for (ChannelCategoryReport paidItem : sentRealResultList) {
            ChannelCategoryReport item = map.get(getConsumerReportKey(paidItem));
            if (item == null) {
                map.put(getConsumerReportKey(paidItem), paidItem);
            } else {
                item.realSalePrice = paidItem.realSalePrice;
                item.realBuyNumber = paidItem.realBuyNumber;
                BigDecimal totalSalesPrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
                BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.channelCost = item.channelCost == null ? BigDecimal.ZERO : item.channelCost.add(paidItem.channelCost);
                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
            }
        }


        //merge other 2
        for (ChannelCategoryReport consumedItem : consumedResultList) {
            ChannelCategoryReport item = map.get(getConsumerReportKey(consumedItem));
            if (item == null) {
                map.put(getConsumerReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }

        for (ChannelCategoryReport refundItem : refundResultList) {
            ChannelCategoryReport item = map.get(getConsumerReportKey(refundItem));
            if (item == null) {
                map.put(getConsumerReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }

        List resultList = new ArrayList();
        for (String key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }


    private static String getReportKey(ChannelCategoryReport refoundItem) {
//        if (refoundItem.code != null) {
        return refoundItem.order.userId + refoundItem.code;
//        } else {
//            return String.valueOf(refoundItem.order.userId);
//        }
    }

    private static String getConsumerReportKey(ChannelCategoryReport refoundItem) {
        return refoundItem.code;
    }
}
