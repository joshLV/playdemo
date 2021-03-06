package models;

import models.sales.Goods;
import models.supplier.Supplier;
import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.Query;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售税表.
 * <p/>
 * User: sujie
 * Date: 5/31/12
 * Time: 10:44 AM
 */
public class SalesOrderItemReport {
    public Supplier supplier;

    public Date createdAt;

    public Goods goods;

    public Long buyCount;

    public Long orderCount;

    public String supplierSalesJobNumber;

    public String supplierName;

    /**
     * 原单价
     */
    public BigDecimal salePrice;

    /**
     * 总金额.
     */
    public BigDecimal originalAmount;

    public BigDecimal tax;

    public BigDecimal noTaxAmount;
    public BigDecimal faceValue;
    public BigDecimal salesAmount;
    public BigDecimal netSalesAmount;
    public BigDecimal refundAmount;

    public SalesOrderItemReport(Supplier supplier, Goods goods, BigDecimal salePrice,
                                Long buyCount, Long orderCount, BigDecimal originalAmount,
                                BigDecimal tax, BigDecimal noTaxAmount) {
        this.supplier = supplier;
        this.goods = goods;
        this.salePrice = salePrice;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
        this.tax = tax;
        this.noTaxAmount = noTaxAmount;
    }


    public SalesOrderItemReport(Goods goods, BigDecimal salePrice, BigDecimal faceValue,
                                Long buyCount, BigDecimal originalAmount) {
        this.supplier = goods.getSupplier();
        this.goods = goods;
        this.faceValue = faceValue;
        this.salePrice = salePrice;
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }

    public SalesOrderItemReport(Long buyCount, BigDecimal originalAmount) {
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }

    public SalesOrderItemReport(Goods goods, BigDecimal amount) {
        this.supplier = goods.getSupplier();
        this.salesAmount = amount;
    }

    public SalesOrderItemReport(BigDecimal amount, Long supplierId) {
        Supplier supplier = Supplier.findById(supplierId);
        this.supplier = supplier;
        this.salesAmount = amount;
    }

    public SalesOrderItemReport(BigDecimal salesAmount, BigDecimal refundAmount, BigDecimal netSalesAmount) {
        this.salesAmount = salesAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
    }

    public static List<SalesOrderItemReport> query(
            SalesOrderItemReportCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.SalesOrderItemReport(r.goods, r.salePrice-r.rebateValue/r.buyNumber,r.faceValue, sum(r.buyNumber), "
                                + "sum(r.salePrice*r.buyNumber-r.rebateValue))"
                                + " from OrderItems r, Supplier s where "
                                + condition.getFilter() + " group by r.goods, r.salePrice-r.rebateValue/r.buyNumber order by r.goods"
                );

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<SalesOrderItemReport> resultList = query.getResultList();

        // 找到退款的张数和总金额，进行扣减
        Query refundQuery = JPA.em()
                .createQuery(
                        "select new models.SalesOrderItemReport(r.goods, r.salePrice-r.rebateValue/r.buyNumber, r.faceValue, count(e), "
                                + "sum(e.salePrice-e.rebateValue))"
                                + " from OrderItems r, Supplier s, ECoupon e where e.orderItems=r and "
                                + condition.getFilter() + " and e.status='REFUND' group by r.goods, r.salePrice-r.rebateValue/r.buyNumber order by r.goods"
                );

        for (String param : condition.getParamMap().keySet()) {
            refundQuery.setParameter(param, condition.getParamMap().get(param));
        }
        List<SalesOrderItemReport> refundList = refundQuery.getResultList();
        Map<String, SalesOrderItemReport> map = new HashMap<>();
        for (SalesOrderItemReport refoundItem : refundList) {
            map.put(getReportKey(refoundItem), refoundItem);
        }

        for (SalesOrderItemReport salesOrderItemReport : resultList) {
            SalesOrderItemReport refundItem = map.get(getReportKey(salesOrderItemReport));
            if (refundItem != null) {
                salesOrderItemReport.buyCount -= refundItem.buyCount;
                salesOrderItemReport.originalAmount = salesOrderItemReport.originalAmount.subtract(refundItem.originalAmount);
                map.remove(getReportKey(salesOrderItemReport));
            }
        }

        //出现以下情况是不可能的，必须有退款记录没有减去
        if (map.size() > 0) {
            for (SalesOrderItemReport item : map.values()) {
                Logger.info(item.goods.name + ":" + item.originalAmount + "退款没有合并！");
            }
            throw new RuntimeException("有退款记录没有减去！");
        }

        return resultList;
    }

    private static String getReportKey(SalesOrderItemReport refoundItem) {
        return refoundItem.goods.id + "." + refoundItem.salePrice;
    }


    public static SalesOrderItemReport summary(List<SalesOrderItemReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new SalesOrderItemReport(0l, BigDecimal.ZERO);
        }
        Long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        for (SalesOrderItemReport item : resultList) {
            buyCount += item.buyCount;
            amount = amount.add(item.originalAmount);
        }
        return new SalesOrderItemReport(buyCount, amount);
    }

    /**
     * 平均单价
     */
    @Transient
    public BigDecimal getPrice() {
        return originalAmount.divide(new BigDecimal(buyCount));
    }

    /**
     * 取得净销售的总计
     *
     * @param resultList
     * @return
     */
    public static SalesOrderItemReport getNetSummary(List<SalesOrderItemReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new SalesOrderItemReport(0l, BigDecimal.ZERO);
        }
        BigDecimal salesAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        for (SalesOrderItemReport item : resultList) {
            salesAmount = salesAmount.add(item.salesAmount);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
        }
        return new SalesOrderItemReport(salesAmount, refundAmount, netSalesAmount);
    }

    /**
     * 取得每天商户的净销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesOrderItemReport> getNetSales(SalesOrderItemReportCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.SalesOrderItemReport(r.goods, sum(r.salePrice*r.buyNumber-r.rebateValue))"
                                + " from OrderItems r,Supplier s where "
                                + condition.getNetSalesFilter() + " group by r.goods.supplierId order by r.goods"
                );
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesOrderItemReport> salesList = query.getResultList();

        //取得退款的数据
        String sql = "select new models.SalesOrderItemReport(sum(e.salePrice),e.orderItems.goods.supplierId) from ECoupon e ,OrderItems r,Supplier s";
        String groupBy = " group by e.orderItems.goods.supplierId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesOrderItemReport> refundList = query.getResultList();

        Map<Supplier, SalesOrderItemReport> map = new HashMap<>();


        //merge
        for (SalesOrderItemReport paidItem : salesList) {
            paidItem.netSalesAmount = paidItem.salesAmount;
            map.put(getReportKeyInNetSales(paidItem), paidItem);
        }

        for (SalesOrderItemReport refundItem : refundList) {
            SalesOrderItemReport item = map.get(getReportKeyInNetSales(refundItem));
            if (item == null) {
                refundItem.refundAmount=refundItem.salesAmount;
                map.put(getReportKeyInNetSales(refundItem), refundItem);
            } else {
                item.refundAmount=refundItem.salesAmount == null ? BigDecimal.ZERO : refundItem.salesAmount;
                item.netSalesAmount = item.salesAmount.subtract(refundItem.salesAmount == null ? BigDecimal.ZERO :
                        refundItem.salesAmount);
            }
        }

        List resultList = new ArrayList();
        for (Supplier key : map.keySet()) {
            resultList.add(map.get(key));
        }
        return resultList;

    }


    private static Supplier getReportKeyInNetSales(SalesOrderItemReport refundItem) {
        return refundItem.supplier;
    }


}
