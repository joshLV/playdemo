package models;

import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Query;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 销售税表.
 * <p/>
 * User: sujie
 * Date: 5/31/12
 * Time: 10:44 AM
 */
public class SalesOrderItemReport extends Model {
    public Supplier supplier;

    public Date createdAt;

    public Goods goods;

    public long buyCount;

    public long orderCount;

    public BigDecimal salePrice;

    public BigDecimal originalAmount;

    public BigDecimal tax;

    public BigDecimal noTaxAmount;
    public BigDecimal faceValue;

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

    public static List<SalesOrderItemReport> query(
            SalesOrderItemReportCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.SalesOrderItemReport(r.goods, r.salePrice,r.faceValue, sum(r.buyNumber), sum(r.salePrice*r.buyNumber))"
                                + " from OrderItems r, Supplier s where "
                                + condition.getFilter() + " group by r.goods, r.salePrice order by r.goods"
                );

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
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
        return null;
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
                        "select new models.SalesOrderItemReport(r.goods, r.salePrice,r.faceValue, sum(r.buyNumber), sum(r.salePrice*r.buyNumber))"
                                + " from OrderItems r, Supplier s where "
                                + condition.getFilter() + " group by r.goods, r.salePrice order by r.goods"
                );

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
    }
}
