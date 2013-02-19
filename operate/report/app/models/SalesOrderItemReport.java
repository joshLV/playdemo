package models;

import models.sales.Goods;
import models.supplier.Supplier;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Query;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.*;

/**
 * 销售税表.
 * <p/>
 * User: sujie
 * Date: 5/31/12
 * Time: 10:44 AM
 */
public class SalesOrderItemReport  {
    public Supplier supplier;

    public Date createdAt;

    public Goods goods;

    public long buyCount;

    public long orderCount;


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
                                long buyCount, long orderCount, BigDecimal originalAmount,
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
                                long buyCount, BigDecimal originalAmount) {
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
        long buyCount = 0l;
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
        String sql = "select new models.SalesOrderItemReport(sum(e.refundPrice),e.orderItems.goods.supplierId) from ECoupon e ,OrderItems r,Supplier s";
        String groupBy = " group by e.orderItems.goods.supplierId";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<SalesOrderItemReport> refundList = query.getResultList();


        for (SalesOrderItemReport sales : salesList) {
            Boolean flag = false;
            for (SalesOrderItemReport refund : refundList) {
                if (sales.supplier.id.equals(refund.supplier.id)) {
                    flag = true;
                    sales.refundAmount = refund.salesAmount == null ? BigDecimal.ZERO : refund.salesAmount;
                    sales.netSalesAmount = sales.salesAmount.subtract(refund.salesAmount == null ? BigDecimal.ZERO :
                            refund.salesAmount);
                }
            }
            if (!flag) {
                sales.netSalesAmount = sales.salesAmount;
            }
        }

        return salesList;
    }


}
