package models;

import models.accounts.AccountType;
import models.order.Order;
import models.order.OrderItems;
import models.supplier.SupplierCategory;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 渠道大类报表
 * <p/>
 * User: wangjia
 * Date: 12-12-21
 * Time: 上午9:55
 */
public class ChannelCategoryReport implements Comparable<ChannelCategoryReport> {
    public Order order;
    public OrderItems orderItems;
    public String[] orderByFields = {"salePrice", "realSalePrice", "refundPrice", "consumedPrice", "grossMargin", "channelCost", "profit"};
    public BigDecimal comparedValue;
    public String orderByType;

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
     * paidAt ecoupon  resaler  total
     */
    public ChannelCategoryReport(Order order,
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

        this.code = "999";
        this.name = "小计";

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


    /**
     * paidAt ecoupon   consumer total
     */
    public ChannelCategoryReport(Order order, BigDecimal salePrice, Long buyNumber, BigDecimal totalCost
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

        this.code = "999";
        this.name = "小计";

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

    //sendAt real   resaler  total
    public ChannelCategoryReport(Order order, Long buyNumber, BigDecimal salePrice, BigDecimal totalCost
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
        this.code = "999";
        this.name = "小计";

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

    //sendAt real consumer total
    public ChannelCategoryReport(Order order, BigDecimal salePrice, BigDecimal totalCost
            , BigDecimal grossMargin, BigDecimal profit, Long buyNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }
        this.code = "999";
        this.name = "小计";

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

    //total
    public ChannelCategoryReport(BigDecimal consumedPrice, Order order, Long consumedNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.code = "999";
        this.name = "小计";

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

    //total
    public ChannelCategoryReport(BigDecimal refundPrice, Long refundNumber, Order order) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.code = "999";
        this.name = "小计";

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
                ", sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b, Supplier s where e.orderItems=r and r.order=o and o.userId=b.id " +
                " and r.goods.supplierId = s.id ";
        String groupBy = " group by r.order.userId, s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ChannelCategoryReport(r.order,s.supplierCategory.id,sum(r.buyNumber) " +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b,Supplier s  where r.order=o and o.userId=b.id  and r.goods.supplierId = s.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber),s.supplierCategory.id,r.order,count(e))" +
                " from OrderItems r, ECoupon e,Supplier s  where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();


        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(e.refundPrice),s.supplierCategory.id, count(e),r.order) " +
                " from OrderItems r, ECoupon e ,Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
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


        //total
        //paidAt ecoupon
        String totalSql = "select new models.ChannelCategoryReport(r.order, sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b,Supplier s where e.orderItems=r and r.order=o and o.userId=b.id" +
                " and r.goods.supplierId = s.id ";
        String totalGroupBy = " group by r.order.userId";
        Query totalQuery = JPA.em()
                .createQuery(totalSql + condition.getFilterPaidAt(AccountType.RESALER) + totalGroupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            totalQuery.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalPaidResultList = totalQuery.getResultList();


        //sendAt real
        totalSql = "select new models.ChannelCategoryReport(r.order,sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id and ";
        totalQuery = JPA.em()
                .createQuery(totalSql + condition.getFilterRealSendAt(AccountType.RESALER) + totalGroupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            totalQuery.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalSentRealResultList = totalQuery.getResultList();


        //consumedAt ecoupon
        totalSql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber),r.order,count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        totalQuery = JPA.em()
                .createQuery(totalSql + condition.getFilterConsumedAt(AccountType.RESALER) + totalGroupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            totalQuery.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalConsumedResultList = totalQuery.getResultList();


        //refundAt ecoupon
        totalSql = "select new models.ChannelCategoryReport(sum(e.refundPrice),count(e),r.order) from OrderItems r, ECoupon e where e.orderItems=r";
        totalQuery = JPA.em()
                .createQuery(totalSql + condition.getFilterRefundAt(AccountType.RESALER) + totalGroupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            totalQuery.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalRefundResultList = totalQuery.getResultList();


        //refundAt real need to do !!!!!


        Map<String, ChannelCategoryReport> totalMap = new HashMap<>();

        //merge ecoupon and real when sales
        for (ChannelCategoryReport paidItem : totalPaidResultList) {
            totalMap.put(getTotalReportKey(paidItem), paidItem);
        }

        for (ChannelCategoryReport paidItem : totalSentRealResultList) {
            ChannelCategoryReport item = totalMap.get(getTotalReportKey(paidItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(paidItem), paidItem);
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
        for (ChannelCategoryReport totalConsumedItem : totalConsumedResultList) {
            ChannelCategoryReport item = totalMap.get(getTotalReportKey(totalConsumedItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(totalConsumedItem), totalConsumedItem);
            } else {
                item.consumedPrice = totalConsumedItem.consumedPrice;
                item.consumedNumber = totalConsumedItem.consumedNumber;
            }
        }

        for (ChannelCategoryReport refundItem : totalRefundResultList) {
            ChannelCategoryReport item = totalMap.get(getTotalReportKey(refundItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }


        List<ChannelCategoryReport> totalResultList = new ArrayList();
        for (String key : totalMap.keySet()) {
            totalResultList.add(totalMap.get(key));
        }

        for (int i = 0; i < totalResultList.size(); i++) {
            switch (totalResultList.get(i).orderByFields[condition.orderByIndex]) {
                case "salePrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).salePrice == null ? BigDecimal.ZERO : totalResultList.get(i).salePrice));
                    break;
                case "realSalePrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).realSalePrice == null ? BigDecimal.ZERO : totalResultList.get(i).totalAmount));
                    break;
                case "refundPrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).refundPrice == null ? BigDecimal.ZERO : totalResultList.get(i).refundPrice));
                    break;
                case "consumedPrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).consumedPrice == null ? BigDecimal.ZERO : totalResultList.get(i).consumedPrice));
                    break;
                case "grossMargin":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).grossMargin == null ? BigDecimal.ZERO : totalResultList.get(i).grossMargin));
                    break;
                case "channelCost":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).channelCost == null ? BigDecimal.ZERO : totalResultList.get(i).channelCost));
                    break;
                case "profit":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).profit == null ? BigDecimal.ZERO : totalResultList.get(i).profit));
                    break;
            }
            totalMap.put(getTotalReportKey(totalResultList.get(i)), totalResultList.get(i));
        }


        //merge total into result
        List<ChannelCategoryReport> resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);
        }
        for (String s : totalMap.keySet()) {
            tempString.add(s);
        }
        Collections.sort(tempString);

//        for (String key : tempString) {
//            if (map.get(key) == null) {
//                resultList.add(map.get(key));
//            } else {
//                resultList.add(totalMap.get(key));
//            }
//        }

        for (String key : tempString) {
            if (map.get(key) != null) {
                resultList.add(map.get(key));
            } else {
                resultList.add(totalMap.get(key));
            }
        }
        for (ChannelCategoryReport c : resultList) {
            c.comparedValue = condition.comparedMap.get(c.loginName);
            c.orderByType = condition.orderByType;
        }
        return resultList;
    }


    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelCategoryReport> excelQuery(
            ChannelCategoryReportCondition condition) {

        //paidAt ecoupon
        String sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id" +
                ", sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice),sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.salePrice-r.rebateValue/r.buyNumber)*b.commissionRatio/100-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e,Order o,Resaler b, Supplier s where e.orderItems=r and r.order=o and o.userId=b.id " +
                " and r.goods.supplierId = s.id ";
        String groupBy = " group by r.order.userId, s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ChannelCategoryReport(r.order,s.supplierCategory.id,sum(r.buyNumber) " +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r,Order o,Resaler b,Supplier s  where r.order=o and o.userId=b.id  and r.goods.supplierId = s.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();

        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber),s.supplierCategory.id,r.order,count(e))" +
                " from OrderItems r, ECoupon e,Supplier s  where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();


        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(e.refundPrice),s.supplierCategory.id, count(e),r.order) " +
                " from OrderItems r, ECoupon e ,Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.RESALER) + groupBy + " order by r.order.userId, s.supplierCategory.id desc");
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

        //merge result
        List resultList = new ArrayList();

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
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelCategoryReport> queryConsumer(ChannelCategoryReportCondition condition) {
        //paidAt ecoupon
        String sql = "select new models.ChannelCategoryReport(min(r.order), s.supplierCategory.id, sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id  ";
        String groupBy = " group by s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ChannelCategoryReport(min(r.order), s.supplierCategory.id, sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r , Supplier s where r.goods.supplierId = s.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();

        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber), s.supplierCategory.id, min(r.order),count(e)) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(e.refundPrice), s.supplierCategory.id, count(e),min(r.order)) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> refundResultList = query.getResultList();
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


        //total
        //paidAt ecoupon
        sql = "select new models.ChannelCategoryReport(min(r.order), sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e where e.orderItems=r  ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalPaidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.ChannelCategoryReport(min(r.order),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber),sum(r.buyNumber)" +
                ") from OrderItems r   where ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + " group by r.order.userType order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalSentRealResultList = query.getResultList();
        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber),min(r.order),count(e)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalConsumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(e.refundPrice),count(e),min(r.order)) from OrderItems r, ECoupon e where e.orderItems=r";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.CONSUMER) + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> totalRefundResultList = query.getResultList();

        //refundAt real need to do !!!!!
        ChannelCategoryReport result = null;
        List<ChannelCategoryReport> totalResultList = new ArrayList<>();
        if (totalPaidResultList != null && totalPaidResultList.size() > 0) {
            result = totalPaidResultList.get(0);
            if (totalSentRealResultList != null && totalSentRealResultList.size() > 0 && totalSentRealResultList.get(0).realBuyNumber > 0) {
                result.realSalePrice = totalSentRealResultList.get(0).realSalePrice;
                result.realBuyNumber = totalSentRealResultList.get(0).realBuyNumber;
                BigDecimal totalSalesPrice = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(totalSentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : totalSentRealResultList.get(0).realSalePrice);
                BigDecimal totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(totalSentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : totalSentRealResultList.get(0).totalCost);
                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    result.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                result.profit = result.salePrice == null ? BigDecimal.ZERO : result.salePrice.add(totalSentRealResultList.get(0).realSalePrice == null ? BigDecimal.ZERO : totalSentRealResultList.get(0).realSalePrice)
                        .subtract(result.totalCost == null ? BigDecimal.ZERO : result.totalCost).subtract(totalSentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : totalSentRealResultList.get(0).totalCost);
                result.totalCost = result.totalCost == null ? BigDecimal.ZERO : result.totalCost.add(totalSentRealResultList.get(0).totalCost == null ? BigDecimal.ZERO : totalSentRealResultList.get(0).totalCost);
//                result.channelCost = result.channelCost.add(totalSentRealResultList.get(0).channelCost);
//                result.profit = result.salePrice.add(totalSentRealResultList.get(0).realSalePrice)
//                        .subtract(result.channelCost)
//                        .subtract(result.totalCost.add(totalSentRealResultList.get(0).totalCost));
            }
            if (totalConsumedResultList != null && totalConsumedResultList.size() > 0) {
                result.consumedPrice = totalConsumedResultList.get(0).consumedPrice;
                result.consumedNumber = totalConsumedResultList.get(0).consumedNumber;
            }
            if (totalRefundResultList != null && totalRefundResultList.size() > 0) {
                result.refundNumber = totalRefundResultList.get(0).refundNumber;
                result.refundPrice = totalRefundResultList.get(0).refundPrice;
            }
            totalResultList.add(result);
        }

        for (int i = 0; i < totalResultList.size(); i++) {
            switch (totalResultList.get(i).orderByFields[condition.orderByIndex]) {
                case "salePrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).salePrice == null ? BigDecimal.ZERO : totalResultList.get(i).salePrice));
                    break;
                case "realSalePrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).realSalePrice == null ? BigDecimal.ZERO : totalResultList.get(i).totalAmount));
                    break;
                case "refundPrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).refundPrice == null ? BigDecimal.ZERO : totalResultList.get(i).refundPrice));
                    break;
                case "consumedPrice":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).consumedPrice == null ? BigDecimal.ZERO : totalResultList.get(i).consumedPrice));
                    break;
                case "grossMargin":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).grossMargin == null ? BigDecimal.ZERO : totalResultList.get(i).grossMargin));
                    break;
                case "channelCost":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).channelCost == null ? BigDecimal.ZERO : totalResultList.get(i).channelCost));
                    break;
                case "profit":
                    condition.comparedMap.put((totalResultList.get(i).loginName == null ? "999" : totalResultList.get(i).loginName), (totalResultList.get(i).profit == null ? BigDecimal.ZERO : totalResultList.get(i).profit));
                    break;
            }
            map.put(getTotalReportKey(totalResultList.get(i)), totalResultList.get(i));
        }

        List<ChannelCategoryReport> resultList = new ArrayList();


        for (String key : map.keySet()) {
            resultList.add(map.get(key));
        }
//        resultList.add(totalResultList.get(0));

        for (ChannelCategoryReport c : resultList) {
            c.comparedValue = condition.comparedMap.get(c.loginName);
            c.orderByType = condition.orderByType;
        }
        return resultList;
    }


    /**
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelCategoryReport> excelQueryConsumer(ChannelCategoryReportCondition condition) {
        //paidAt ecoupon
        String sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id, sum(r.salePrice-r.rebateValue/r.buyNumber),count(r.buyNumber)" +
                ",sum(r.originalPrice)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ") from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id  ";
        String groupBy = " group by s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> paidResultList = query.getResultList();


        //sendAt real
        sql = "select new models.ChannelCategoryReport(r.order, s.supplierCategory.id, sum(r.buyNumber),sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                ",sum(r.originalPrice*r.buyNumber)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice*r.buyNumber)" +
                ") from OrderItems r , Supplier s where r.goods.supplierId = s.id and ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> sentRealResultList = query.getResultList();

        //consumedAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(r.salePrice-r.rebateValue/r.buyNumber), s.supplierCategory.id, r.order,count(e)) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue/r.buyNumber) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> consumedResultList = query.getResultList();

        //refundAt ecoupon
        sql = "select new models.ChannelCategoryReport(sum(e.refundPrice), s.supplierCategory.id, count(e),r.order) " +
                " from OrderItems r, ECoupon e , Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelCategoryReport> refundResultList = query.getResultList();
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
        if (refoundItem.code != null) {

            return refoundItem.order.userId + refoundItem.code;
        } else {
            return String.valueOf(refoundItem.order.userId) + "00";
        }
    }

    private static String getTotalReportKey(ChannelCategoryReport refoundItem) {
        if (refoundItem.code != null && refoundItem.order != null) {
            return refoundItem.order.userId + refoundItem.code;
        } else {
            if (refoundItem.order != null) {
                return String.valueOf(refoundItem.order.userId) + "999";
            } else {
                return "999999999" + "999";
            }
        }
    }

    private static String getConsumerReportKey(ChannelCategoryReport refoundItem) {
        return refoundItem.code;
    }

    @Override
    public int compareTo(ChannelCategoryReport arg) {
        switch (this.orderByType) {
            case "2":
                return (arg.comparedValue == null ? BigDecimal.ZERO : arg.comparedValue).compareTo(this.comparedValue == null ? BigDecimal.ZERO : this.comparedValue);
            case "1":
                return (this.comparedValue == null ? BigDecimal.ZERO : this.comparedValue).compareTo(arg.comparedValue == null ? BigDecimal.ZERO : arg.comparedValue);
        }
        return 0;
    }
}
