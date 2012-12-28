package models;

import models.admin.OperateUser;
import models.order.ECouponStatus;
import models.sales.Goods;
import models.supplier.SupplierCategory;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-28
 * Time: 上午11:52
 * To change this template use File | Settings | File Templates.
 */
public class CategorySalesReport {

    public Goods goods;

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
     * 渠道成本
     */
    public BigDecimal channelCost;


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

    //total
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


    //from resaler
    public CategorySalesReport(Goods goods, Long supplierCategoryId, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.goods = goods;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //from resaler total
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


    //refund ecoupon
    public CategorySalesReport(BigDecimal refundAmount, Goods goods, Long supplierCategoryId) {
        this.refundAmount = refundAmount;
        this.goods = goods;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = supplierCategory.name;
        }
    }

    //refund ecoupon  total
    public CategorySalesReport(BigDecimal refundAmount, Long supplierCategoryId) {
        this.refundAmount = refundAmount;
        if (supplierCategoryId != null) {
            SupplierCategory supplierCategory = SupplierCategory.findById(supplierCategoryId);
            this.code = supplierCategory.code;
            this.name = "999";
        } else {
            this.name = "999";
        }
    }


    public CategorySalesReport(Long buyNumber, BigDecimal originalAmount) {
        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public CategorySalesReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount
            , BigDecimal grossMargin, BigDecimal channelCost, BigDecimal profit) {
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
        this.grossMargin = grossMargin;
        this.channelCost = channelCost;
        this.profit = profit;
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


        //from resaler
        sql = "select new models.CategorySalesReport(r.goods,s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b, Supplier s ";
        groupBy = " group by s.supplierCategory.id,r.goods.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }


        List<CategorySalesReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.CategorySalesReport(sum(e.refundPrice),e.orderItems.goods,s.supplierCategory.id ) " +
                " from ECoupon e, Supplier s ";
        groupBy = " group by s.supplierCategory.id,e.orderItems.goods.id ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> refundList = query.getResultList();

        Map<String, CategorySalesReport> map = new HashMap<>();

        //merge
        for (CategorySalesReport paidItem : paidResultList) {
            map.put(getReportKey(paidItem), paidItem);
        }

        for (CategorySalesReport refundItem : refundList) {
            CategorySalesReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (CategorySalesReport resalerItem : paidResalerResultList) {
            CategorySalesReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
//                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
            }
        }

        //total
        List<CategorySalesReport> tempTotal = queryTotal(condition);
        Map<String, CategorySalesReport> totalMap = new HashMap<>();


        for (int i = 0; i < tempTotal.size(); i++) {
            totalMap.put(getTotalReportKey(tempTotal.get(i)), tempTotal.get(i));
        }

        List resultList = new ArrayList();

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

        //from resaler
        sql = "select new models.CategorySalesReport(s.supplierCategory.id,sum(r.salePrice*r.buyNumber-r.rebateValue),sum(r.originalPrice*r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-sum(r.originalPrice*r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b, Supplier s ";
        groupBy = " group by s.supplierCategory.id,b ";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }


        List<CategorySalesReport> totalPaidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.CategorySalesReport(sum(e.refundPrice),s.supplierCategory.id ) " +
                " from ECoupon e, Supplier s ";
        groupBy = " group by s.supplierCategory.id ";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<CategorySalesReport> totalRefundList = query.getResultList();

        Map<String, CategorySalesReport> totalMap = new HashMap<>();

        //merge
        for (CategorySalesReport paidItem : totalPaidResultList) {
            totalMap.put(getTotalReportKey(paidItem), paidItem);
        }

        for (CategorySalesReport refundItem : totalRefundList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                totalMap.put(getTotalReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (CategorySalesReport resalerItem : totalPaidResalerResultList) {
            CategorySalesReport item = totalMap.get(getTotalReportKey(resalerItem));
            if (item == null) {
                totalMap.put(getTotalReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.profit);
//                item.profit= item.totalAmount.multiply(BigDecimal.ONE.subtract())
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

        for (CategorySalesReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);

            totolSalePrice = totolSalePrice.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            totalCost = totalCost.add(item.totalCost == null ? BigDecimal.ZERO : item.totalCost);
            channelCost = channelCost.add(item.channelCost == null ? BigDecimal.ZERO : item.channelCost);
            profit = profit.add(item.profit == null ? BigDecimal.ZERO : item.profit);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
        }

        if (totolSalePrice.compareTo(BigDecimal.ZERO) != 0) {
            grossMargin = totolSalePrice.subtract(totalCost).divide(totolSalePrice, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return new CategorySalesReport(totalAmount, refundAmount, netSalesAmount, grossMargin, channelCost, profit);
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


}
