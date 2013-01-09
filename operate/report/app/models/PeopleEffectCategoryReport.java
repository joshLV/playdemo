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
    public BigDecimal totalRefundPrice = BigDecimal.ZERO;

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

    /**
     * 净利润
     */
    public BigDecimal profit;

    public OperateUser operateUser;
    public Goods goods;

    public PeopleEffectCategoryReport(OperateUser operateUser, Long supplierCategoryId, Long buyNumber,
                                      BigDecimal salePrice, BigDecimal grossMargin, BigDecimal profit, BigDecimal totalCost) {
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
        this.totalCost = totalCost;
    }

    public PeopleEffectCategoryReport(Long supplierCategoryId, OperateUser operateUser, Long buyNumber,
                                      BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal totalCost) {
        this.operateUser = operateUser;
        this.realBuyNumber = buyNumber;
        this.realSalePrice = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.totalCost = totalCost;
    }

    //from resaler
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal profit) {
        this.operateUser = operateUser;
        this.profit = profit;
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

    public PeopleEffectCategoryReport(OperateUser operateUser, Long buyNumber,
                                      BigDecimal totalAmount, BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount) {
        this.operateUser = operateUser;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.code = "999";
    }

    //from resaler
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.operateUser = operateUser;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.code = "999";
    }

    public PeopleEffectCategoryReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal consumedAmount, BigDecimal profit, Long totalBuyNumber) {
        this.totalAmount = totalAmount;
        this.consumedPrice = consumedAmount;
        this.profit = profit;
        this.refundPrice = refundAmount;
        this.totalNumber = totalBuyNumber;
        this.code = "999";
    }

    //refund and consumed ecoupon
    public PeopleEffectCategoryReport(OperateUser operateUser, BigDecimal amount, Long buyNumber, Goods goods, ECouponStatus status) {
        this.operateUser = operateUser;
        if (status == ECouponStatus.REFUND) {
            this.totalRefundPrice = amount;
            this.refundNumber = buyNumber;
        } else if (status == ECouponStatus.CONSUMED) {
            this.totalConsumedPrice = amount;
            this.consumedNumber = buyNumber;
        }

        this.goods = goods;
        this.code = "999";
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
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                ") from OrderItems r,Supplier s,OperateUser ou";
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

        //total
        List<PeopleEffectCategoryReport> totalPeopleEffectList = queryPeopleEffectData(condition);
        for (PeopleEffectCategoryReport totalItem : totalPeopleEffectList) {
            PeopleEffectCategoryReport item = map.get(getReportKey(totalItem));
            if (item == null) {
                map.put(getReportKey(totalItem), totalItem);
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

    /**
     * 取得按销售人员统计的销售记录
     *
     * @param condition
     * @return
     */
    private static List<PeopleEffectCategoryReport> queryPeopleEffectData(PeopleEffectCategoryReportCondition condition) {
        //毛利率= （总的销售额-总成本（进价*数量）/总销售额

        //paidAt
        String sql = "select new models.PeopleEffectCategoryReport(o,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Supplier s,OperateUser o";
        String groupBy = " group by s.salesId";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> paidResultList = query.getResultList();

        //from resaler
        sql = "select new models.PeopleEffectCategoryReport(ou,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s,OperateUser ou";
        groupBy = " group by s.salesId,b";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilterOfPeopleEffect() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<PeopleEffectCategoryReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.PeopleEffectCategoryReport(o,sum(e.refundPrice),count(e.id),e.orderItems.goods,e.status) from ECoupon e,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.REFUND) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> refundList = query.getResultList();
        Map<OperateUser, PeopleEffectCategoryReport> map = new HashMap<>();
        //merge
        for (PeopleEffectCategoryReport paidItem : paidResultList) {
            map.put(getReportKeyOfPeopleEffect(paidItem), paidItem);
        }

        for (PeopleEffectCategoryReport refundItem : refundList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(refundItem));
            if (item != null) {
                item.totalRefundPrice = refundItem.totalRefundPrice;
                item.refundNumber = refundItem.refundNumber;
            } else {
                map.put(getReportKeyOfPeopleEffect(refundItem), refundItem);
            }
        }
        //取得消费的数据 ecoupon
        sql = "select new models.PeopleEffectCategoryReport(o,sum(e.salePrice),count(e.id),e.orderItems.goods,e.status) from ECoupon e,Supplier s,OperateUser o ";
        groupBy = " group by s.salesId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilterOfPeopleEffect(ECouponStatus.CONSUMED) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<PeopleEffectCategoryReport> consumedList = query.getResultList();

        for (PeopleEffectCategoryReport consumedItem : consumedList) {
            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(consumedItem));
            if (item != null) {
                item.totalConsumedPrice = consumedItem.totalConsumedPrice;
                item.consumedNumber = consumedItem.consumedNumber;
            } else {
                map.put(getReportKeyOfPeopleEffect(consumedItem), consumedItem);
            }

        }
        //merge from resaler if commissionRatio
        for (PeopleEffectCategoryReport resalerItem : paidResalerResultList) {

            PeopleEffectCategoryReport item = map.get(getReportKeyOfPeopleEffect(resalerItem));
            if (item == null) {
                map.put(getReportKeyOfPeopleEffect(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount.subtract(resalerItem.totalCost)).add(resalerItem.profit);
            }
        }

        List resultList = new ArrayList();
        for (OperateUser key : map.keySet()) {
            resultList.add(map.get(key));
        }
        return resultList;
    }

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
        BigDecimal totolSalePrice = BigDecimal.ZERO;
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
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            totolSalePrice = totolSalePrice.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice.add(item.realSalePrice == null ? BigDecimal.ZERO : item.realSalePrice));
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
        }
        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
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


