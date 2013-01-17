package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.sales.Goods;
import models.supplier.SupplierCategory;
import org.apache.commons.collections.IterableMap;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * 大类销售报表
 * <p/>
 * User: wangjia
 * Date: 12-12-28
 * Time: 上午11:52
 */
public class CategorySalesReport implements Comparable<CategorySalesReport> {


    public Goods goods;

    public BigDecimal comparedValue;
    public String[] orderByFields = {"buyNumber", "totalAmount", "cheatedOrderAmount", "refundAmount", "consumedAmount", "netSalesAmount", "grossMargin", "profit"};

    public String orderByType;

    /**
     * 商户类别
     */
    public String code;
    public String name;


    /**
     * 平均售价
     */
    public BigDecimal avgSalesPrice;


    /**
     * 毛利率
     */
    public BigDecimal grossMargin;
    /**
     * 进价
     */
    public BigDecimal originalPrice;
    /**
     * 销售数量
     */
    public Long buyNumber;
    /**
     * 售出总金额
     */
    public BigDecimal totalAmount;

    /**
     * 退款金额
     */
    public BigDecimal refundAmount;

    /**
     * 消费金额
     */
    public BigDecimal consumedAmount;


    /**
     * 消费金额汇总
     */
    public BigDecimal summaryConsumed;

    /**
     * 利润
     */
    public BigDecimal profit;
    public BigDecimal netSalesAmount;
    public BigDecimal totalCost;
    public BigDecimal ratio;
    public BigDecimal originalAmount;

    /**
     * 总销售数量
     */
    public Long totalBuyNumber;

    public OperateUser operateUser;

    /**
     * 退款成本
     */
    public BigDecimal refundCost;

    /**
     * 渠道成本
     */
    public BigDecimal channelCost;

    /**
     * 刷单金额
     */
    public BigDecimal cheatedOrderAmount;

    /**
     * 刷单量
     */
    public Long cheatedOrderNum;

    /**
     * 刷单成本
     */
    public BigDecimal cheatedOrderCost;

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

    //paidAt
    public CategorySalesReport(Goods goods, Long supplierCategoryId, BigDecimal originalPrice, Long buyNumber,
                               BigDecimal totalAmount, BigDecimal avgSalesPrice,
                               BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount
            , BigDecimal totalCost) {
        this.goods = goods;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.originalPrice = originalPrice;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
    }

    //paidAt total
    public CategorySalesReport(Long supplierCategoryId, Long buyNumber,
                               BigDecimal totalAmount,
                               BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount
            , BigDecimal totalCost) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
        this.totalCost = totalCost;
    }


    //padiAt from resaler  total
    public CategorySalesReport(Long supplierCategoryId, BigDecimal ratio, Goods goods, BigDecimal totalAmountCommissionAmount) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.ratio = ratio;
        this.goods = goods;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
    }

    //padiAt from resaler
    public CategorySalesReport(Long supplierCategoryId, Goods goods, BigDecimal totalAmountCommissionAmount, BigDecimal ratio) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.goods = goods;
        this.totalAmountCommissionAmount = totalAmountCommissionAmount;
    }

    //cheated order from resaler  total
    public CategorySalesReport(BigDecimal cheatedOrderCommissionAmount, BigDecimal ratio, Goods goods, Long supplierCategoryId) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.ratio = ratio;
        this.goods = goods;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //cheated order from resaler
    public CategorySalesReport(BigDecimal cheatedOrderCommissionAmount, Long supplierCategoryId, BigDecimal ratio, Goods goods) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.goods = goods;
        this.cheatedOrderCommissionAmount = cheatedOrderCommissionAmount;
    }

    //refund from resaler
    public CategorySalesReport(BigDecimal refundCommissionAmount, Goods goods, Long supplierCategoryId, BigDecimal ratio, BigDecimal refundCost) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.ratio = ratio;
        this.goods = goods;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundCost = refundCost;
    }

    //refund from resaler  total
    public CategorySalesReport(BigDecimal refundCommissionAmount, Goods goods, BigDecimal ratio, Long supplierCategoryId, BigDecimal refundCost) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.ratio = ratio;
        this.goods = goods;
        this.refundCommissionAmount = refundCommissionAmount;
        this.refundCost = refundCost;
    }

    //paidAt from resaler total
    public CategorySalesReport(Long supplierCategoryId, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //cheated order total
    public CategorySalesReport(Goods goods, Long supplierCategoryId, BigDecimal cheatedOrderAmount, BigDecimal cheatedOrderCost) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.goods = goods;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderCost = cheatedOrderCost;
    }

    //cheated order
    public CategorySalesReport(Goods goods, Long supplierCategoryId, BigDecimal cheatedOrderAmount, Long cheatedOrderNum, BigDecimal cheatedOrderCost) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.goods = goods;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.cheatedOrderNum = cheatedOrderNum;
        this.cheatedOrderCost = cheatedOrderCost;
    }

    //consumedAt ecoupon
    public CategorySalesReport(Long supplierCategoryId, BigDecimal consumedAmount, Goods goods) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.goods = goods;
        this.consumedAmount = consumedAmount;
    }

    //consumedAt total ecoupon
    public CategorySalesReport(Long supplierCategoryId, Goods goods, BigDecimal consumedAmount) {
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.goods = goods;
        this.consumedAmount = consumedAmount;
    }

    //refund ecoupon
    public CategorySalesReport(BigDecimal refundAmount, Long supplierCategoryId, Goods goods, BigDecimal refundCost) {
        this.refundAmount = refundAmount;
        this.goods = goods;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.refundCost = refundCost;
    }


    //refund ecoupon  total
    public CategorySalesReport(BigDecimal refundAmount, Long supplierCategoryId, BigDecimal refundCost) {
        this.refundAmount = refundAmount;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
        this.refundCost = refundCost;
    }


    public CategorySalesReport(Long buyNumber, BigDecimal originalAmount) {
        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public CategorySalesReport(BigDecimal summaryConsumed, BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit, BigDecimal cheatedOrderAmount) {
        this.summaryConsumed = summaryConsumed;
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
        this.cheatedOrderAmount = cheatedOrderAmount;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<CategorySalesReport> query(CategorySalesReportCondition condition) {
        //paidAt
        String sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                " )" +
                " from OrderItems r, Supplier s ";
        String groupBy = " group by s.supplierCategory.id, r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> paidResultList = query.getResultList();


//        //from resaler
//        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
//                ",b.commissionRatio)" +
//                " from OrderItems r,Order o,Resaler b, Supplier s ";
//        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
//        query = JPA.em()
//                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//
//        List<CategorySalesReport> paidResalerResultList = query.getResultList();

        //paidAt from resaler
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s ";
        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> paidResalerResultList = query.getResultList();


        //cheated order
        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and ";
        groupBy = " group by s.supplierCategory.id,r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> cheatedOrderResultList = query.getResultList();

        //cheated order from resaler
        sql = "select new models.CategorySalesReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,r.goods,s.supplierCategory.id)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s  where e.orderItems=r and";
        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResaler() + groupBy + " order by sum(r.buyNumber) desc ");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> cheatedOrderResalerResultList = query.getResultList();


        //consumedAt coupon
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),r.goods) " +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        groupBy = " group by s.supplierCategory.id,r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> consumedResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.CategorySalesReport(sum(e.refundPrice),s.supplierCategory.id,e.orderItems.goods,sum(e.originalPrice)) " +
                " from ECoupon e, Supplier s ";
        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundList = query.getResultList();

        //refund from resaler
        sql = "select new models.CategorySalesReport(sum(e.refundPrice)*b.commissionRatio/100,r.goods,s.supplierCategory.id,b.commissionRatio,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s ";
        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResaler() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundResalerResultList = query.getResultList();

        Map<String, CategorySalesReport> map = new HashMap<>();

        //merge
        for (CategorySalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (CategorySalesReport cheatedItem : cheatedOrderResultList) {
            CategorySalesReport item = map.get(getReportKey(cheatedItem));
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

        for (CategorySalesReport consumedItem : consumedResultList) {
            CategorySalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }
        for (CategorySalesReport refundItem : refundList) {
            CategorySalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        //merge from resaler if commissionRatio
//        for (CategorySalesReport resalerItem : paidResalerResultList) {
//            CategorySalesReport item = map.get(getReportKey(resalerItem));
//            if (item == null) {
//                map.put(getReportKey(resalerItem), resalerItem);
//            } else {
//                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
//                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
//                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
////                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
//            }
//        }

        BigDecimal totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport resalerItem : paidResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport cheatedResalerItem : cheatedOrderResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(cheatedResalerItem));
            if (item == null) {
                map.put(getReportKey(cheatedResalerItem), cheatedResalerItem);
            } else {
                totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
                totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
                item.cheatedOrderCommissionAmount = totalCommission;

                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }
        totalCommission = BigDecimal.ZERO;

        for (CategorySalesReport refundResalerItem : refundResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;

                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        //total
        List<CategorySalesReport> tempTotal = queryTotal(condition);
        Map<String, CategorySalesReport> totalMap = new HashMap<>();
        Map<String, BigDecimal> comparedMap = new HashMap<>();
        for (int i = 0; i < tempTotal.size(); i++) {
            switch (tempTotal.get(i).orderByFields[condition.orderByIndex]) {
                case "buyNumber":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).buyNumber == null ? BigDecimal.ZERO : BigDecimal.valueOf(tempTotal.get(i).buyNumber)));
                    break;
                case "totalAmount":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).totalAmount == null ? BigDecimal.ZERO : tempTotal.get(i).totalAmount));
                    break;
                case "cheatedOrderAmount":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).cheatedOrderAmount == null ? BigDecimal.ZERO : tempTotal.get(i).cheatedOrderAmount));
                    break;
                case "refundAmount":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).refundAmount == null ? BigDecimal.ZERO : tempTotal.get(i).refundAmount));
                    break;
                case "consumedAmount":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).consumedAmount == null ? BigDecimal.ZERO : tempTotal.get(i).consumedAmount));
                    break;
                case "netSalesAmount":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).netSalesAmount == null ? BigDecimal.ZERO : tempTotal.get(i).netSalesAmount));
                    break;
                case "grossMargin":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).grossMargin == null ? BigDecimal.ZERO : tempTotal.get(i).grossMargin));
                    break;
                case "profit":
                    comparedMap.put((tempTotal.get(i).code == null ? "999" : tempTotal.get(i).code), (tempTotal.get(i).profit == null ? BigDecimal.ZERO : tempTotal.get(i).profit));
                    break;
            }

            totalMap.put(getTotalReportKey(tempTotal.get(i)), tempTotal.get(i));
        }

        List<CategorySalesReport> resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);
        }
        for (String s : totalMap.keySet()) {
            tempString.add(s);
        }

        Collections.sort(tempString);

        for (String key : tempString) {
            if (map.get(key) != null) {
                resultList.add(map.get(key));
            } else {
                resultList.add(totalMap.get(key));
            }
        }

        for (CategorySalesReport c : resultList) {
            c.comparedValue = comparedMap.get(c.code);
            c.orderByType = condition.orderByType;
        }
        Collections.sort(resultList);
        return resultList;
    }


    public static List<CategorySalesReport> excelQuery(CategorySalesReportCondition condition) {
//        //paidAt
//        String sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,r.goods.originalPrice,sum(r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
//                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
//                ",sum(r.originalPrice*r.buyNumber) " +
//                " )" +
//                " from OrderItems r, Supplier s ";
//        String groupBy = " group by s.supplierCategory.id, r.goods.id";
//        Query query = JPA.em()
//                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//        List<CategorySalesReport> paidResultList = query.getResultList();
//
//
//        //from resaler
//        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
//                ",b.commissionRatio)" +
//                " from OrderItems r,Order o,Resaler b, Supplier s ";
//        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
//        query = JPA.em()
//                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//
//        List<CategorySalesReport> paidResalerResultList = query.getResultList();
//
//        //取得退款的数据 ecoupon
//        sql = "select new models.CategorySalesReport(sum(e.refundPrice),e.orderItems.goods,s.supplierCategory.id ) " +
//                " from ECoupon e, Supplier s ";
//        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id ";
//
//        query = JPA.em()
//                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");
//
//        for (String param : condition.getParamMap1().keySet()) {
//            query.setParameter(param, condition.getParamMap1().get(param));
//        }
//
//        List<CategorySalesReport> refundList = query.getResultList();
//
//        Map<String, CategorySalesReport> map = new HashMap<>();
//
//        //merge
//        for (CategorySalesReport paidItem : paidResultList) {
//            map.put(getReportKey(paidItem), paidItem);
//        }
//
//        for (CategorySalesReport refundItem : refundList) {
//            CategorySalesReport item = map.get(getReportKey(refundItem));
//            if (item == null) {
//                Goods goods = Goods.findById(refundItem.goods.id);
//                refundItem.originalPrice = goods.originalPrice;
//                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
//                map.put(getReportKey(refundItem), refundItem);
//            } else {
//                item.refundAmount = refundItem.refundAmount;
//                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
//            }
//        }
//
//        //merge from resaler if commissionRatio
//        for (CategorySalesReport resalerItem : paidResalerResultList) {
//            CategorySalesReport item = map.get(getReportKey(resalerItem));
//            if (item == null) {
//                map.put(getReportKey(resalerItem), resalerItem);
//            } else {
//                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
//                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
//                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
////                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
//            }
//        }
//
//
//        List resultList = new ArrayList();
//
//        List<String> tempString = new ArrayList<>();
//        for (String s : map.keySet()) {
//            tempString.add(s);
//        }
//
//        Collections.sort(tempString);
//
//        for (String key : tempString) {
//            resultList.add(map.get(key));
//        }


        //paidAt
        String sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,r.goods.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                " )" +
                " from OrderItems r, Supplier s ";
        String groupBy = " group by s.supplierCategory.id, r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> paidResultList = query.getResultList();


//        //from resaler
//        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
//                ",b.commissionRatio)" +
//                " from OrderItems r,Order o,Resaler b, Supplier s ";
//        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
//        query = JPA.em()
//                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//
//        List<CategorySalesReport> paidResalerResultList = query.getResultList();

        //paidAt from resaler
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b,Supplier s ";
        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> paidResalerResultList = query.getResultList();


        //cheated order
        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.buyNumber)" +
                " ,sum(r.originalPrice)) " +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and ";
        groupBy = " group by s.supplierCategory.id,r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> cheatedOrderResultList = query.getResultList();

        //cheated order from resaler
        sql = "select new models.CategorySalesReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,b.commissionRatio,r.goods,s.supplierCategory.id)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s  where e.orderItems=r and";
        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResaler() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> cheatedOrderResalerResultList = query.getResultList();


        //consumedAt coupon
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),r.goods) " +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        groupBy = " group by s.supplierCategory.id,r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> consumedResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.CategorySalesReport(sum(e.refundPrice),s.supplierCategory.id,e.orderItems.goods,sum(e.originalPrice)) " +
                " from ECoupon e, Supplier s ";
        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundList = query.getResultList();

        //refund from resaler
        sql = "select new models.CategorySalesReport(sum(e.refundPrice)*b.commissionRatio/100,r.goods,s.supplierCategory.id,b.commissionRatio,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s ";
        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResaler() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundResalerResultList = query.getResultList();

        Map<String, CategorySalesReport> map = new HashMap<>();

        //merge
        for (CategorySalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (CategorySalesReport cheatedItem : cheatedOrderResultList) {
            CategorySalesReport item = map.get(getReportKey(cheatedItem));
            if (item == null) {
                Goods goods = Goods.findById(cheatedItem.goods.id);
                cheatedItem.originalPrice = goods.originalPrice;
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderCost == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderCost);
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

        for (CategorySalesReport consumedItem : consumedResultList) {
            CategorySalesReport item = map.get(getReportKey(consumedItem));
            if (item == null) {
                map.put(getReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }
        for (CategorySalesReport refundItem : refundList) {
            CategorySalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        //merge from resaler if commissionRatio
//        for (CategorySalesReport resalerItem : paidResalerResultList) {
//            CategorySalesReport item = map.get(getReportKey(resalerItem));
//            if (item == null) {
//                map.put(getReportKey(resalerItem), resalerItem);
//            } else {
//                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
//                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
//                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
////                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
//            }
//        }

        BigDecimal totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport resalerItem : paidResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport cheatedResalerItem : cheatedOrderResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(cheatedResalerItem));
            if (item == null) {
                map.put(getReportKey(cheatedResalerItem), cheatedResalerItem);
            } else {
                totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
                totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
                item.cheatedOrderCommissionAmount = totalCommission;

                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }
        totalCommission = BigDecimal.ZERO;

        for (CategorySalesReport refundResalerItem : refundResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(refundResalerItem));
            if (item == null) {
                map.put(getReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;

                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

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
     * 取得净销售的总计
     *
     * @param condition
     * @return
     */

    public static List<CategorySalesReport> queryTotal(CategorySalesReportCondition condition) {
        //paidAt
        String sql = "select new models.CategorySalesReport(s.supplierCategory.id,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.originalPrice*r.buyNumber) " +
                " )" +
                " from OrderItems r, Supplier s ";
        String groupBy = " group by s.supplierCategory.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> totalPaidResultList = query.getResultList();

//        //from resaler
//        sql = "select new models.CategorySalesReport(s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
//                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
//                ",b.commissionRatio)" +
//                " from OrderItems r,Order o,Resaler b, Supplier s ";
//        groupBy = " group by s.supplierCategory.id,b ";
//        query = JPA.em()
//                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");
//
//
//        for (String param : condition.getParamMap().keySet()) {
//            query.setParameter(param, condition.getParamMap().get(param));
//        }
//
//
//        List<CategorySalesReport> totalPaidResalerResultList = query.getResultList();

        //paidAt from resaler
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,b.commissionRatio,r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100)" +
                " from OrderItems r,Order o,Resaler b,Supplier s ";
        groupBy = " group by s.supplierCategory.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> paidResalerResultList = query.getResultList();

        //cheated order
        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice-r.rebateValue/r.buyNumber),sum(r.originalPrice))" +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and ";
        groupBy = " group by s.supplierCategory.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrder() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> cheatedOrderResultList = query.getResultList();


        //cheated order from resaler
        sql = "select new models.CategorySalesReport(sum(r.salePrice*r.buyNumber-r.rebateValue)*b.commissionRatio/100,s.supplierCategory.id,b.commissionRatio,r.goods)" +
                " from OrderItems r,Order o,Resaler b, ECoupon e,Supplier s  where e.orderItems=r and";
        groupBy = " group by s.supplierCategory.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterCheatedOrderResaler() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<CategorySalesReport> cheatedOrderResalerResultList = query.getResultList();


        //consumedAt coupon
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,r.goods,sum(r.salePrice-r.rebateValue/r.buyNumber)) " +
                " from OrderItems r, ECoupon e,Supplier s where e.orderItems=r and r.goods.supplierId = s.id ";
        groupBy = " group by s.supplierCategory.id ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAt() + groupBy + " order by sum(r.salePrice*r.buyNumber-r.rebateValue) desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<CategorySalesReport> consumedResultList = query.getResultList();


        //取得退款的数据 ecoupon
        sql = "select new models.CategorySalesReport(sum(e.refundPrice),s.supplierCategory.id,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e, Supplier s ";
        groupBy = " group by s.supplierCategory.id ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> totalRefundList = query.getResultList();


        //refund from resaler
        sql = "select new models.CategorySalesReport(sum(e.refundPrice)*b.commissionRatio/100,r.goods,b.commissionRatio,s.supplierCategory.id,sum(e.orderItems.originalPrice)) " +
                " from ECoupon e,OrderItems r,Resaler b ,Order o,Supplier s ";
        groupBy = " group by s.supplierCategory.id,b";

        query = JPA.em()
                .createQuery(sql + condition.getFilterRefundResaler() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundResalerResultList = query.getResultList();


        Map<String, CategorySalesReport> totalMap = new HashMap<>();

        //merge
        for (CategorySalesReport paidItem : totalPaidResultList) {
            totalMap.put(getTotalReportKey(paidItem), paidItem);
        }

        for (CategorySalesReport cheatedItem : cheatedOrderResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(cheatedItem));
            if (item == null) {
                Goods goods = Goods.findById(cheatedItem.goods.id);
                cheatedItem.originalPrice = goods.originalPrice;
                cheatedItem.netSalesAmount = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderCost == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderCost);
                cheatedItem.profit = BigDecimal.ZERO.subtract(cheatedItem.cheatedOrderAmount).subtract(cheatedItem.cheatedOrderCost);
                totalMap.put(getTotalReportKey(cheatedItem), cheatedItem);
            } else {
                item.cheatedOrderAmount = cheatedItem.cheatedOrderAmount;
                item.cheatedOrderCost = cheatedItem.cheatedOrderCost;
                item.netSalesAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(cheatedItem.cheatedOrderAmount == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(cheatedItem.cheatedOrderCost == null ? BigDecimal.ZERO : cheatedItem.cheatedOrderCost);
            }
        }

        for (CategorySalesReport consumedItem : consumedResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(consumedItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(consumedItem), consumedItem);
            } else {
                item.consumedAmount = consumedItem.consumedAmount;
            }
        }
        for (CategorySalesReport refundItem : totalRefundList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(refundItem));
            if (item == null) {
//                Goods goods = Goods.findById(refundItem.goods.id);
//                refundItem.originalPrice = goods.originalPrice;
//                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                refundItem.profit = BigDecimal.ZERO.subtract(refundItem.refundAmount).add(refundItem.refundCost);
                totalMap.put(getTotalReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCost = refundItem.refundCost;
                item.netSalesAmount = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).setScale(2);
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        BigDecimal totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport resalerItem : paidResalerResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(resalerItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(resalerItem), resalerItem);
            } else {
                totalCommission = item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount;
                totalCommission = totalCommission.add(resalerItem.totalAmountCommissionAmount == null ? BigDecimal.ZERO : resalerItem.totalAmountCommissionAmount);
                item.totalAmountCommissionAmount = totalCommission;
                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);
            }
        }

        totalCommission = BigDecimal.ZERO;
        for (CategorySalesReport cheatedResalerItem : cheatedOrderResalerResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(cheatedResalerItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(cheatedResalerItem), cheatedResalerItem);
            } else {
                totalCommission = item.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : item.cheatedOrderCommissionAmount;
                totalCommission = totalCommission.add(cheatedResalerItem.cheatedOrderCommissionAmount == null ? BigDecimal.ZERO : cheatedResalerItem.cheatedOrderCommissionAmount);
                item.cheatedOrderCommissionAmount = totalCommission;

                item.profit = item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount.subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }
        totalCommission = BigDecimal.ZERO;

        for (CategorySalesReport refundResalerItem : refundResalerResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(refundResalerItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(refundResalerItem), refundResalerItem);
            } else {
                totalCommission = item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount;
                totalCommission = totalCommission.add(refundResalerItem.refundCommissionAmount == null ? BigDecimal.ZERO : refundResalerItem.refundCommissionAmount);
                item.refundCommissionAmount = totalCommission;

                item.profit = (item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount).subtract(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount).subtract(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount)
                        .subtract(item.totalAmountCommissionAmount == null ? BigDecimal.ZERO : item.totalAmountCommissionAmount).add(item.refundCommissionAmount == null ? BigDecimal.ZERO : item.refundCommissionAmount)
                        .subtract(item.totalCost == null ? BigDecimal.ZERO : item.totalCost).add(item.refundCost == null ? BigDecimal.ZERO : item.refundCost).add(item.cheatedOrderCost == null ? BigDecimal.ZERO : item.cheatedOrderCost);

            }
        }


        List<CategorySalesReport> resultList = new ArrayList();
        for (String key : totalMap.keySet()) {
            resultList.add(totalMap.get(key));
        }
        return resultList;
    }

    public static CategorySalesReport getNetSummary(List<CategorySalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new CategorySalesReport(0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal totolSalePrice = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal channelCost = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal summaryConsumed = BigDecimal.ZERO;
        BigDecimal cheatedOrderAmount = BigDecimal.ZERO;

        for (CategorySalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            summaryConsumed = summaryConsumed.add(item.consumedAmount == null ? BigDecimal.ZERO : item.consumedAmount);
            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            cheatedOrderAmount = cheatedOrderAmount.add(item.cheatedOrderAmount == null ? BigDecimal.ZERO : item.cheatedOrderAmount);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new CategorySalesReport(summaryConsumed.setScale(2, 4), totalAmount.setScale(2, 4), refundAmount.setScale(2, 4), netSalesAmount.setScale(2, 4), grossMargin, channelCost.setScale(2, 4), profit.setScale(2, 4), cheatedOrderAmount.setScale(2, 4));
    }


    private static String getReportKey(CategorySalesReport refoundItem) {
        if (refoundItem.code != null && refoundItem.goods != null) {
            return refoundItem.code + refoundItem.goods.id;
        } else {
            return "999" + String.valueOf(refoundItem.goods.id);
        }
    }

    private static String getTotalReportKey(CategorySalesReport refoundItem) {
        return refoundItem.code + "999";
    }


    @Override
    public int compareTo(CategorySalesReport arg) {
        switch (this.orderByType) {
            case "2":
                return (arg.comparedValue == null ? BigDecimal.ZERO : arg.comparedValue).compareTo(this.comparedValue == null ? BigDecimal.ZERO : this.comparedValue);
            case "1":
                return (this.comparedValue == null ? BigDecimal.ZERO : this.comparedValue).compareTo(arg.comparedValue == null ? BigDecimal.ZERO : arg.comparedValue);
        }
        return 0;
    }
}
