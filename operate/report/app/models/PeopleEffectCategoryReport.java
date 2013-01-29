package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.supplier.SupplierCategory;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人效大类报表
 * <p/>
 * User:yanjy
 */
public class PeopleEffectCategoryReport {
    public Order order;
    public OrderItems orderItems;

    /**
     * 帐号
     */
    public String loginName;
    public String userName;

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
    public long totalRefundNumber = 0l;

    /**
     * 售出实物数
     */
    public long realBuyNumber = 0l;
    public long totalRealBuyNumber = 0l;
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
    public BigDecimal totalRefundPrice = BigDecimal.ZERO;

    /**
     * 售出实物金额
     */
    public BigDecimal realSalePrice = BigDecimal.ZERO;
    public BigDecimal totalRealSalePrice = BigDecimal.ZERO;
    /**
     * 退款实物金额
     */
    public BigDecimal realRefundPrice = BigDecimal.ZERO;

    /**
     * 消费券数
     */
    public long consumedNumber = 0l;
    public long totalConsumedNumber = 0l;

    /**
     * 消费金额
     */
    public BigDecimal consumedPrice = BigDecimal.ZERO;
    public BigDecimal totalConsumedPrice = BigDecimal.ZERO;

    /**
     * 总销售额
     */
    public BigDecimal totalAmount;
    public Long totalNumber;
    /**
     * 总成本
     */
    public BigDecimal totalCost;

    /**
     * 毛利率
     */
    public BigDecimal grossMargin;
    public BigDecimal totalGrossMargin;
    /**
     * 净利润
     */
    public BigDecimal profit;
    public BigDecimal netProfit;

    public OperateUser operateUser;
    public Goods goods;
    public BigDecimal ratio;

    /**
     * 总销售额佣金成本
     */
    public BigDecimal totalAmountCommissionAmount = BigDecimal.ZERO;
    public BigDecimal amountCommissionAmount = BigDecimal.ZERO;
    /**
     * 退款佣金成本
     */
    public BigDecimal refundCommissionAmount = BigDecimal.ZERO;
    /**
     * 退款成本
     */
    public BigDecimal refundCost;
    /**
     * 刷单佣金成本
     */
    public BigDecimal cheatedOrderCommissionAmount = BigDecimal.ZERO;
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
     * 商品成本
     */
    public BigDecimal goodsCost;

    public PeopleEffectCategoryReport(OperateUser operateUser, Long supplierCategoryId, Long buyNumber,
                                      BigDecimal salePrice, BigDecimal grossMargin, BigDecimal profit, BigDecimal cost) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.salePrice = salePrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.goodsCost = cost;
    }

    public PeopleEffectCategoryReport(Long supplierCategoryId, OperateUser operateUser, Long buyNumber,
                                      BigDecimal realSalePrice, BigDecimal grossMargin, BigDecimal profit, BigDecimal totalCost) {
        this.operateUser = operateUser;
        this.realBuyNumber = buyNumber;
        this.realSalePrice = realSalePrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.goodsCost = totalCost;
    }

    //padiAt from resaler
    public PeopleEffectCategoryReport(Long supplierCategoryId, OperateUser operateUser, BigDecimal totalAmountCommissionAmount, BigDecimal ratio) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.amountCommissionAmount = totalAmountCommissionAmount;
    }

    //cheated order
    public PeopleEffectCategoryReport(OperateUser operateUser, Long supplierCategoryId, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
    }


    //cheated order from resaler
    public PeopleEffectCategoryReport(BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio, OperateUser operateUser, Long supplierCategoryId) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //refund and consumed from ecoupon
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal amount, Long supplierCategoryId, BigDecimal refundCost, Long number, ECouponStatus status) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        if (status == ECouponStatus.REFUND) {
            this.refundPrice = amount;
            this.refundNumber = number;
            this.refundCost = refundCost;
        } else if (status == ECouponStatus.CONSUMED) {
            this.consumedPrice = amount;
            this.consumedNumber = number;
        }
    }

    //refund from resaler
    public PeopleEffectCategoryReport(BigDecimal refundCommissionAmount, OperateUser operateUser, Long supplierCategoryId, BigDecimal ratio, BigDecimal refundCost) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundCost = refundCost;
    }

    public PeopleEffectCategoryReport(Long totalNumber, BigDecimal amount, long realTotalNumber, BigDecimal realAmount, BigDecimal totalRefundPrice, Long refundNumber,
                                      BigDecimal consumedPrice, Long consumedNumber, BigDecimal grossMargin, BigDecimal profit) {
        this.totalNumber = totalNumber;
        this.totalAmount = amount;
        this.realSalePrice = realAmount;
        this.realBuyNumber = realTotalNumber;
        this.refundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.grossMargin = grossMargin;
        this.profit = profit;
    }

    //total ==
    public PeopleEffectCategoryReport(OperateUser operateUser, Long buyNumber,
                                      BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal totalCost) {
        this.operateUser = operateUser;
        this.totalNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.totalGrossMargin = grossMargin;
        this.netProfit = profit;
        this.totalCost = totalCost;
        this.code = "999";
    }

    //total ==
    public PeopleEffectCategoryReport(Long buyNumber, OperateUser operateUser,
                                      BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal totalCost) {
        this.operateUser = operateUser;
        this.totalRealBuyNumber = buyNumber;
        this.totalRealSalePrice = totalAmount;
        this.totalGrossMargin = grossMargin;
        this.netProfit = profit;
        this.totalCost = totalCost;
        this.code = "999";
    }

    //total ==paidAt from resaler
    public PeopleEffectCategoryReport(BigDecimal totalAmountCommissionAmount, BigDecimal ratio, OperateUser operateUser) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
        this.code = "999";
    }

    //total ==
    public PeopleEffectCategoryReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal consumedAmount, BigDecimal profit, Long totalBuyNumber) {
        this.totalAmount = totalAmount;
        this.consumedPrice = consumedAmount;
        this.netProfit = profit;
        this.refundPrice = refundAmount;
        this.totalNumber = totalBuyNumber;
        this.code = "999";
    }

    //total ==refund and consumed ecoupon
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal amount, BigDecimal refundCost, Long buyNumber, ECouponStatus status) {
        this.operateUser = operateUser;
        if (status == ECouponStatus.REFUND) {
            this.totalRefundPrice = amount;
            this.totalRefundNumber = buyNumber;
            this.refundCost = refundCost;
        } else if (status == ECouponStatus.CONSUMED) {
            this.totalConsumedPrice = amount;
            this.totalConsumedNumber = buyNumber;
        }

        this.code = "999";
    }

    //total ==refund from resaler
    public PeopleEffectCategoryReport(BigDecimal refundCommissionAmount, OperateUser operateUser, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.refundCommissionAmount = refundCommissionAmount;
        this.code = "999";

    }

    //total ==cheated order from resaler
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio) {
        this.ratio = ratio;
        this.operateUser = operateUser;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
        this.code = "999";
    }

    //total ==cheated order
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        this.operateUser = operateUser;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
        this.code = "999";
    }

    public PeopleEffectCategoryReport() {
    }


    /**
     * 人效大类报表统计
     *
     * @param condition
     * @return
     */
    public static List<PeopleEffectCategoryReport> query(
            PeopleEffectCategoryReportCondition condition) {
        //paidAt orderItems
        String sql = "select new models.PeopleEffectCategoryReport(ou,s.supplierCategory.id" +
                ", count(r.buyNumber),sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ",sum(r.originalPrice) " +
                ") from OrderItems r,Supplier s, ECoupon e,OperateUser ou";
        String groupBy = " group by s.salesId, s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.PeopleEffectCategoryReport(s.supplierCategory.id,ou,sum(r.buyNumber) " +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                ") from OrderItems r,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> sentRealResultList = query.getResultList();
        //cheated order
        sql = "select new models.PeopleEffectCategoryReport(ou,s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderOfPeopleEffect() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> cheatedOrderResultList = query.getResultList();
        //paidAt from resaler
        sql = "select new models.PeopleEffectCategoryReport(s.supplierCategory.id,ou,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId, s.supplierCategory.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> paidResalerResultList = query.getResultList();

        //cheated order from resaler
        sql = "select new models.PeopleEffectCategoryReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,ou,s.supplierCategory.id)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResalerOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> cheatedOrderResalerResultList = query.getResultList();
        //refund from resaler
        sql = "select new models.PeopleEffectCategoryReport(sum(e.refundPrice)*b.commissionRatio/100,ou,s.supplierCategory.id,b.commissionRatio,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResalerOfPeopleEffect() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> refundResalerResultList = query.getResultList();
        //consumedAt ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.salePrice),s.supplierCategory.id,sum(r.originalPrice),count(e),e.status)" +
                " from ECoupon e,OrderItems r,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId,s.supplierCategory.id";
        query = JPA.em()
                .createQuery(sql + condition.getECouponFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<PeopleEffectCategoryReport> consumedResultList = query.getResultList();


        //refundAt ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.refundPrice),s.supplierCategory.id,sum(r.originalPrice),count(e),e.status)" +
                " from ECoupon e,OrderItems r,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getECouponFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }
        List<PeopleEffectCategoryReport> refundResultList = query.getResultList();

        //refundAt real need to do !!!!!
        Map<String, PeopleEffectCategoryReport> map = new HashMap<>();

        //merge ecoupon and real when sales
        for (PeopleEffectCategoryReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (PeopleEffectCategoryReport paidItem : sentRealResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(paidItem));
            if (item == null) {
                map.put(getReportKey(paidItem), paidItem);
            } else {
                setRealGoodsItem(paidItem, item);
            }

        }
//        for (PeopleEffectCategoryReport cheatedItem : cheatedOrderResultList) {
//            PeopleEffectCategoryReport item = map.get(getReportKey(cheatedItem));
//            if (item == null) {
////                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
//                map.put(getReportKey(cheatedItem), cheatedItem);
//            } else {
//                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
//                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
////                item.profit = item.salePrice.subtract(cheatedItem.cheatedOrderAmount)
////                        .subtract(item.goodsCost).add(cheatedItem.cheatedOrderCost);
//            }
//        }

        for (PeopleEffectCategoryReport refundItem : refundResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(refundItem));
            if (item == null) {
//                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.totalRefundPrice).add(refundItem.refundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
                item.refundCost = refundItem.refundCost;
                setRefundItem(item);
            }
        }
        //merge other 2
        for (PeopleEffectCategoryReport consumedItem : consumedResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedPrice = consumedItem.consumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            }
        }
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport resalerItem : paidResalerResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(resalerItem));

            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                setPaidResalerItem(resalerItem, item);
            }
        }

        totalCommission = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport cheatedResalerItem : cheatedOrderResalerResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(cheatedResalerItem));
            if (item == null) {
                map.put(getReportKey(cheatedResalerItem), cheatedResalerItem);
            } else {
                setCheatedOrderResaler(cheatedResalerItem, item);

            }
        }
        totalCommission = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport refundResalerItem : refundResalerResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                setRefundResalerItem(refundResalerItem, item);
            }
        }

        //total
        List<PeopleEffectCategoryReport> totalPeopleEffectList = queryPeopleEffectData(condition);
        for (PeopleEffectCategoryReport totalItem : totalPeopleEffectList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(totalItem));
            if (item == null) {
                map.put(getReportKey(totalItem), totalItem);
            }
        }

        //merge total into result
        List<PeopleEffectCategoryReport> resultList = new ArrayList();

        mergeResultList(condition, map, resultList);

        return resultList;
    }

    /**
     * 设置实物的净利润，毛利率，成本
     *
     * @param paidItem
     * @param item
     */
    private static void setRealGoodsItem(PeopleEffectCategoryReport paidItem, PeopleEffectCategoryReport item) {
        item.realSalePrice = paidItem.realSalePrice;
        item.realBuyNumber = paidItem.realBuyNumber;
        BigDecimal totalSalesPrice = (item.salePrice == null ? BigDecimal.ZERO : item.salePrice).add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
        BigDecimal goodsCost = (item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost).add(paidItem.goodsCost == null ? BigDecimal.ZERO : paidItem.goodsCost);
        if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
            item.grossMargin = totalSalesPrice.subtract(goodsCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        item.profit = totalSalesPrice.subtract(goodsCost);
        item.goodsCost = goodsCost;
    }

    private static void setRefundResalerItem(PeopleEffectCategoryReport refundResalerItem, PeopleEffectCategoryReport item) {
        BigDecimal refundCommissionAmount = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
        BigDecimal totalCommission = refundCommissionAmount.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
        item.refundCommissionAmount = totalCommission;

        BigDecimal salePrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice;
        BigDecimal refundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
        BigDecimal goodsCost = item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
        BigDecimal amountCommissionAmount = item.amountCommissionAmount == null ? BigDecimal.ZERO : item.amountCommissionAmount;
//        item.profit = salePrice.subtract(cheatedOrderAmount).subtract(refundPrice)
//                .subtract(amountCommissionAmount).add(totalCommission)
//                .subtract(goodsCost).add(refundCost).add(cheatedOrderCost);
    }

    /**
     * @param resalerItem
     * @param item
     */
    private static void setPaidResalerItem(PeopleEffectCategoryReport resalerItem, PeopleEffectCategoryReport item) {
        BigDecimal amountCommissionAmount = item.amountCommissionAmount == null ? BigDecimal.ZERO : item.amountCommissionAmount;
        BigDecimal totalCommission = amountCommissionAmount.add(resalerItem.amountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.amountCommissionAmount);
        item.amountCommissionAmount = totalCommission;
        BigDecimal goodsCost = item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost;
        BigDecimal totalSalesPrice = (item.salePrice == null ? BigDecimal.ZERO : item.salePrice).add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice);
        item.profit = totalSalesPrice.subtract(totalCommission).subtract(goodsCost);
    }

    private static void setRefundItem(PeopleEffectCategoryReport item) {
        BigDecimal salePrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice;
        BigDecimal refundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
        BigDecimal goodsCost = item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
//        item.profit = salePrice.subtract(refundPrice).subtract(goodsCost).add(refundCost);
    }


    /**
     * 设置大类刷单信息
     *
     * @param cheatedResalerItem
     * @param item
     */
    private static void setCheatedOrderResaler(PeopleEffectCategoryReport cheatedResalerItem, PeopleEffectCategoryReport item) {
        BigDecimal totalCommission;
        totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
        totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
        item.cheatedOrderCommissionAmount = totalCommission;

        BigDecimal salePrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice;
        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
        BigDecimal refundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
        BigDecimal amountCommissionAmount = item.amountCommissionAmount == null ? BigDecimal.ZERO : item.amountCommissionAmount;
        BigDecimal refundCommissionAmount = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
        BigDecimal goodsCost = item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
//        item.profit = salePrice.subtract(cheatedOrderAmount).subtract(refundPrice)
//                .subtract(amountCommissionAmount).add(refundCommissionAmount)
//                .subtract(goodsCost).add(refundCost).add(cheatedOrderCost);
    }

    private static void mergeResultList(PeopleEffectCategoryReportCondition condition, Map<String, PeopleEffectCategoryReport> map, List<PeopleEffectCategoryReport> resultList) {
        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);
        }
        Collections.sort(tempString);


        PeopleEffectCategoryReport reportItem = null;
        Map<Long, PeopleEffectCategoryReport> sortMap = new HashMap<>();
        for (String key : tempString) {
            reportItem = map.get(key);
            if (reportItem == null) {
                break;
            }
            if ("999".equals(reportItem.code)) {
                sortMap.put(reportItem.operateUser.id, reportItem);
            }
            resultList.add(reportItem);
        }
        //指定相同的operateUser，添加排序项
        for (PeopleEffectCategoryReport item : resultList) {
            Long operateUserId = item.operateUser.id;
            if (sortMap.keySet().contains(operateUserId)) {
                item.totalAmount = sortMap.get(operateUserId).totalAmount;
                item.totalConsumedPrice = sortMap.get(operateUserId).totalConsumedPrice;
                item.totalRefundPrice = sortMap.get(operateUserId).totalRefundPrice;
                item.netProfit = sortMap.get(operateUserId).netProfit;
                item.totalGrossMargin = sortMap.get(operateUserId).totalGrossMargin;
            }
        }
        condition.sort(resultList);
    }

    /**
     * 取得按销售人员统计的大类统计
     *
     * @param condition
     * @return
     */
    private static List<PeopleEffectCategoryReport> queryPeopleEffectData(PeopleEffectCategoryReportCondition condition) {
        //毛利率= （总的销售额-总成本（进价*数量）/总销售额
        //total== paidAt
        String sql = "select new models.PeopleEffectCategoryReport(ou,count(r.buyNumber)" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)" +
                ",(sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue/r.buyNumber)*100" +
                ",sum(r.salePrice-r.rebateValue/r.buyNumber)-sum(r.originalPrice)" +
                ",sum(r.originalPrice))" +
                " from OrderItems r,Supplier s,ECoupon e, OperateUser ou";
        String groupBy = " group by s.salesId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by sum(r.buyNumber) desc ");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> paidResultList = query.getResultList();
        //total== sendAt real
        sql = "select new models.PeopleEffectCategoryReport(sum(r.buyNumber),ou,sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                ") from OrderItems r,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> sentRealResultList = query.getResultList();

        //total== cheated order
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e ,Supplier s,OperateUser ou";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderOfPeopleEffect() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> cheatedOrderResultList = query.getResultList();

        //total== cheated order from resaler
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResalerOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> cheatedOrderResalerResultList = query.getResultList();
        //total == paidAt from resaler
        sql = "select new models.PeopleEffectCategoryReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,ou)" +
                " from OrderItems r,Order o,Resaler b,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> paidResalerResultList = query.getResultList();

        //total ==取得退款的数据 ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.refundPrice),sum(r.originalPrice),count(e.id),e.status) from ECoupon e,OrderItems r,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getECouponFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> refundList = query.getResultList();


        //total== 取得消费的数据 ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.salePrice),sum(r.originalPrice),count(e.id),e.status) from ECoupon e,OrderItems r,Supplier s,OperateUser ou ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getECouponFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> consumedList = query.getResultList();
        //total== refund from resaler
        sql = "select new models.PeopleEffectCategoryReport(sum(e.refundPrice)*b.commissionRatio/100,ou,b.commissionRatio) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResalerOfPeopleEffect() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> refundResalerResultList = query.getResultList();

        Map<OperateUser, PeopleEffectCategoryReport> map = new HashMap<>();
        //merge
        for (PeopleEffectCategoryReport paidItem : paidResultList) {
            map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
        }
        for (PeopleEffectCategoryReport paidItem : sentRealResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(paidItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
            } else {
                setTotalRealGoodsItem(paidItem, item);
            }

        }
//        for (PeopleEffectCategoryReport cheatedItem : cheatedOrderResultList) {
//            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(cheatedItem));
//            if (item == null) {
////                cheatedItem.netProfit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
//                map.put(getReportKeyOfPeopleEffect(cheatedItem), cheatedItem);
//            } else {
//                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
//                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
////                item.netProfit = item.totalAmount.subtract(cheatedItem.cheatedOrderAmount)
////                        .subtract(item.totalCost).add(cheatedItem.cheatedOrderCost);
//            }
//        }

        for (PeopleEffectCategoryReport refundItem : refundList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item == null) {
//                refundItem.netProfit = BigDecimal.ZERO.subtract(refundItem.totalRefundPrice).add(refundItem.refundCost);
                map.put(getReportKeyOfPeopleEffect(refundItem), refundItem);
            } else {
                setTotalRefundItem(refundItem, item);
            }
        }

        for (PeopleEffectCategoryReport consumedItem : consumedList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(consumedItem));
            if (item != null) {
                item.totalConsumedPrice = consumedItem.totalConsumedPrice;
                item.totalConsumedNumber = consumedItem.totalConsumedNumber;
            } else {
                map.put(getReportKeyOfPeopleEffect(consumedItem), consumedItem);
            }

        }
        //merge from resaler if commissionRatio
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport resalerItem : paidResalerResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(resalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(resalerItem), resalerItem);
            } else {
                setTotalPaidResalerItem(resalerItem, item);
            }
        }


//        totalCommission = BigDecimal.ZERO;
//        for (PeopleEffectCategoryReport cheatedResalerItem : cheatedOrderResalerResultList) {
//            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(cheatedResalerItem));
//            if (item == null) {
//                map.put(getReportKeyOfPeopleEffect(cheatedResalerItem), cheatedResalerItem);
//            } else {
//                setTotalCheatedOrderItem(cheatedResalerItem, item);
//
//            }
//        }
        totalCommission = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport refundResalerItem : refundResalerResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(refundResalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(refundResalerItem), refundResalerItem);
            } else {
                setTotalRefundResalerItem(refundResalerItem, item);
            }
        }
        List resultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 设置实物的净利润，毛利率，成本
     *
     * @param paidItem
     * @param item
     */
    private static void setTotalRealGoodsItem(PeopleEffectCategoryReport paidItem, PeopleEffectCategoryReport item) {
        item.totalRealSalePrice = paidItem.totalRealSalePrice;
        item.totalRealBuyNumber = paidItem.totalRealBuyNumber;
        BigDecimal totalSalesPrice = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.add(paidItem.totalRealSalePrice == null ? BigDecimal.ZERO : paidItem.totalRealSalePrice);
        BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
        if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
            item.totalGrossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        item.netProfit = totalSalesPrice.subtract(totalCost);
    }

    /**
     * 小计： 设置退款的退款金额等信息
     */
    private static void setTotalRefundItem(PeopleEffectCategoryReport refundItem, PeopleEffectCategoryReport item) {
        item.totalRefundPrice = refundItem.totalRefundPrice;
        item.totalRefundNumber = refundItem.totalRefundNumber;
        item.refundCost = refundItem.refundCost;
        BigDecimal totalAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount;
        BigDecimal totalRefundPrice = item.totalRefundPrice == null ? BigDecimal.ZERO : item.totalRefundPrice;
        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
        BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost;
        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        //净利润
//        item.netProfit = totalAmount.subtract(totalRefundPrice).subtract(cheatedOrderAmount)
//                .subtract(totalCost).add(cheatedOrderCost).add(refundCost);
    }

    /**
     * 小计： 设置分销成交后，给一百券的佣金等信息
     */
    private static void setTotalPaidResalerItem(PeopleEffectCategoryReport resalerItem, PeopleEffectCategoryReport item) {
        BigDecimal amountCommissionAmount = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
        BigDecimal totalCommission = amountCommissionAmount.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
        item.totalAmountCommissionAmount = totalCommission;

        BigDecimal totalAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount;
//        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
//        BigDecimal totalRefundPrice = item.totalRefundPrice == null ? BigDecimal.ZERO : item.totalRefundPrice;
//        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
//        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        BigDecimal totalSalesPrice = totalAmount.add(resalerItem.totalRealSalePrice == null ? BigDecimal.ZERO : resalerItem.totalRealSalePrice);
        BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost);

        item.netProfit = totalSalesPrice.subtract(totalCommission).subtract(totalCost);
    }

    /**
     * 小计： 设置刷单的金额等信息
     */
    private static void setTotalCheatedOrderItem(PeopleEffectCategoryReport cheatedResalerItem, PeopleEffectCategoryReport item) {
        BigDecimal cheatedOrderCommissionAmount = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
        BigDecimal totalCommission = cheatedOrderCommissionAmount.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
        item.cheatedOrderCommissionAmount = totalCommission;

        BigDecimal totalAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount;
        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
        BigDecimal totalRefundPrice = item.totalRefundPrice == null ? BigDecimal.ZERO : item.totalRefundPrice;
        BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost;
        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        BigDecimal totalAmountCommissionAmount = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
//        item.netProfit = totalAmount.subtract(cheatedOrderAmount).subtract(totalRefundPrice)
//                .subtract(totalAmountCommissionAmount).add(cheatedOrderCommissionAmount)
//                .subtract(totalCost).add(refundCost).add(cheatedOrderCost);
    }

    /**
     * 小计： 设置分销退款的金额等信息
     */
    private static void setTotalRefundResalerItem(PeopleEffectCategoryReport refundResalerItem, PeopleEffectCategoryReport item) {
        BigDecimal refundCommissionAmount = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
        BigDecimal totalCommission = refundCommissionAmount.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
        item.refundCommissionAmount = totalCommission;
        BigDecimal totalAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount;
        BigDecimal cheatedOrderAmount = item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount;
        BigDecimal totalRefundPrice = item.totalRefundPrice == null ? BigDecimal.ZERO : item.totalRefundPrice;
        BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost;
        BigDecimal cheatedOrderCost = item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost;
        BigDecimal refundCost = item.refundCost == null ? BigDecimal.ZERO : item.refundCost;
        BigDecimal totalAmountCommissionAmount = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;

//        item.netProfit = totalAmount.subtract(cheatedOrderAmount).subtract(totalRefundPrice)
//                .subtract(totalAmountCommissionAmount).add(refundCommissionAmount)
//                .subtract(totalCost).add(refundCost).add(cheatedOrderCost);
    }

    /**
     * 统计大类报表
     *
     * @param resultList
     * @return
     */
    public static PeopleEffectCategoryReport summary(List<PeopleEffectCategoryReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new PeopleEffectCategoryReport(0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
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
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal totalSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (PeopleEffectCategoryReport item : resultList) {

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
            totalCost = totalCost.add(item.goodsCost == null ? BigDecimal.ZERO : item.goodsCost);
            totalSalePrice = totalSalePrice.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice).add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice);

            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
        }
        if (totalSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totalSalePrice.subtract(totalCost).divide(totalSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new PeopleEffectCategoryReport(buyCount, amount, realBuyCount, realAmount, refundPrice, refundCount, consumedPrice, consumedCount, grossMargin, profit);
    }

    private static String getReportKey(PeopleEffectCategoryReport refundItem) {
        if (refundItem.code != null) {
            return refundItem.operateUser + refundItem.code;
        } else {
            return String.valueOf(refundItem.operateUser) + "00";
        }
    }

    private static OperateUser getReportKeyOfPeopleEffect(PeopleEffectCategoryReport item) {
        return item.operateUser;
    }
}


