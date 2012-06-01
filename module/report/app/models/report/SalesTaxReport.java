package models.report;

import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 销售税表.
 * <p/>
 * User: sujie
 * Date: 5/31/12
 * Time: 10:44 AM
 */
@Entity
@Table(name = "report_daily_sales_tax")
public class SalesTaxReport extends Model {
    @ManyToOne
    public Supplier supplier;

    @Column(name = "created_at")
    public Date createdAt;

    @ManyToOne
    public Goods goods;

    @Column(name = "level_price_name")
    @Enumerated(EnumType.STRING)
    public GoodsLevelPriceName levelPriceName;

    @Column(name = "buy_count")
    public long buyCount;

    @Column(name = "order_count")
    public long orderCount;

    @Column(name = "original_amount")
    public BigDecimal originalAmount;

    public BigDecimal tax;

    @Column(name = "no_tax_amount")
    public BigDecimal noTaxAmount;

    public SalesTaxReport(Supplier supplier, Goods goods, GoodsLevelPriceName levelPriceName,
                          long buyCount, long orderCount, BigDecimal originalAmount,
                          BigDecimal tax, BigDecimal noTaxAmount) {
        this.supplier = supplier;
        this.goods = goods;
        this.levelPriceName = levelPriceName;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
        this.tax = tax;
        this.noTaxAmount = noTaxAmount;
    }

    public static JPAExtPaginator<SalesTaxReport> query(ReportCondition condition, int pageNumber,
                                                        int pageSize) {
        JPAExtPaginator<SalesTaxReport> page = new JPAExtPaginator<>("SalesTaxReport r",
                "new SalesTaxReport(r.supplier,r.goods, r.levelPriceName, sum(r.buyCount), " +
                        "sum(r.orderCount), sum(r.originalAmount), sum(r.tax),sum(r.noTaxAmount))",
                SalesTaxReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.supplier,r.goods,r.levelPriceName")
                .orderBy("r.supplier,r.goods,r.levelPriceName");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.originalAmount),sum(r.tax),sum(r.noTaxAmount) " +
                "from SalesTaxReport r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        Object[] summary = (Object[]) q.getSingleResult();
        if (summary == null || summary[0] == null) {
            return new ReportSummary(0, 0, BigDecimal.ZERO);
        }
        return new ReportSummary((Long) summary[0], 0, (BigDecimal) summary[1], (BigDecimal) summary[2],
                (BigDecimal) summary[3]);
    }

    /**
     * 平均单价
     */
    @Transient
    public BigDecimal getPrice() {
        return originalAmount.divide(new BigDecimal(buyCount));
    }
}
