package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
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
     * 净利润
     */
    public BigDecimal profit;

    public OperateUser operateUser;

    public PeopleEffectCategoryReport(OperateUser operateUser, Long supplierCategoryId, Long buyNumber,
                                      BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
    }

    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal amount, Long supplierCategoryId, Long number, ECouponStatus status) {
        this.operateUser = operateUser;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }

        if (status == ECouponStatus.REFUND) {
            this.refundPrice = amount;
            this.refundNumber = number;
        } else if (status == ECouponStatus.CONSUMED) {
            this.consumedPrice = amount;
            this.consumedNumber = number;
        }
    }


    public PeopleEffectCategoryReport() {
    }


    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<PeopleEffectCategoryReport> query(
            PeopleEffectCategoryReportCondition condition) {
        //paidAt orderItems
        String sql = "select new models.PeopleEffectCategoryReport(ou,s.supplierCategory.id" +
                ", sum(r.buyNumber),sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice-r.rebateValue)-sum(r.originalPrice)" +
                ") from OrderItems r,Supplier s,OperateUser ou";
        String groupBy = " group by s.salesId, s.supplierCategory.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPaidAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> paidResultList = query.getResultList();

        //sendAt real
        sql = "select new models.PeopleEffectCategoryReport(ou,s.supplierCategory.id,sum(r.buyNumber) " +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice))/sum(r.salePrice-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.salePrice-r.rebateValue)-sum(r.originalPrice)" +
                ") from OrderItems r,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRealSendAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> sentRealResultList = query.getResultList();


        //consumedAt ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.salePrice),s.supplierCategory.id,count(e),e.status)" +
                " from ECoupon e,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<PeopleEffectCategoryReport> consumedResultList = query.getResultList();


        //refundAt ecoupon
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(e.refundPrice),s.supplierCategory.id,count(e),e.status)" +
                " from ECoupon e,Supplier s,OperateUser ou ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundAt() + groupBy + " order by s.supplierCategory.id desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
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
                item.realSalePrice = paidItem.realSalePrice;
                item.realBuyNumber = paidItem.realBuyNumber;
                BigDecimal totalSalesPrice = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice);
                BigDecimal totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                if (totalSalesPrice.compareTo(BigDecimal.ZERO) != 0) {
                    item.grossMargin = totalSalesPrice.subtract(totalCost).divide(totalSalesPrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                }

                item.profit = item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(paidItem.realSalePrice == null ? BigDecimal.ZERO : paidItem.realSalePrice)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).subtract(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);

                item.totalCost = item.totalCost == null ? BigDecimal.ZERO : item.totalCost.add(paidItem.totalCost == null ? BigDecimal.ZERO : paidItem.totalCost);
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

        for (PeopleEffectCategoryReport refundItem : refundResultList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundPrice = refundItem.refundPrice;
                item.refundNumber = refundItem.refundNumber;
            }
        }

        //merge total into result
        List resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);
        }
        Collections.sort(tempString);


        for (String key : tempString) {
            if (map.get(key) != null) {
                resultList.add(map.get(key));
            }
        }

        return resultList;
    }

    private static String getReportKey(PeopleEffectCategoryReport refundItem) {
        if (refundItem.code != null) {

            return refundItem.operateUser + refundItem.code;
        } else {
            return String.valueOf(refundItem.operateUser) + "00";
        }
    }

    private static String getTotalReportKey(PeopleEffectCategoryReport refundItem) {
        if (refundItem.code != null) {
            return refundItem.operateUser + refundItem.code;
        } else {
            return String.valueOf(refundItem.order.userId) + "999";
        }
    }

    private static String getConsumerReportKey(PeopleEffectCategoryReport refundItem) {
        return refundItem.code;
    }
}
