package models.report;

import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购税表.
 * <p/>
 * <p/>
 * User: sujie
 * Date: 5/30/12
 * Time: 4:08 PM
 */
@Entity
@Table(name = "report_daily_purchase_tax")
public class PurchaseTaxReport extends Model {
    @ManyToOne
    public Supplier supplier;

    @Column(name = "created_at")
    public Date createdAt;

    @ManyToOne
    public Goods goods;

    @Column(name = "buy_count")
    public long buyCount;

    @Column(name = "order_count")
    public long orderCount;

    @Column(name = "original_amount")
    public BigDecimal originalAmount;

    public BigDecimal tax;

    @Column(name = "no_tax_amount")
    public BigDecimal noTaxAmount;

    public PurchaseTaxReport(Supplier supplier,Goods goods, long buyCount, BigDecimal originalAmount,
                             BigDecimal tax,BigDecimal noTaxAmount) {
        this.supplier = supplier;
        this.goods = goods;
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = tax;
        this.noTaxAmount = noTaxAmount;
    }

    public static JPAExtPaginator<PurchaseTaxReport> query(ReportCondition condition, int pageNumber,
                                                           int pageSize) {
        JPAExtPaginator<PurchaseTaxReport> page = new JPAExtPaginator<>("PurchaseTaxReport r",
                "new PurchaseTaxReport(r.supplier,r.goods, sum(r.buyCount), sum(r.originalAmount), " +
                        "sum(r.tax),sum(r.noTaxAmount))",
                PurchaseTaxReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.supplier,r.goods").orderBy("r.supplier,r.goods");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.originalAmount),sum(r.tax),sum(r.noTaxAmount) " +
                "from PurchaseTaxReport r where " + condition.getFilter());
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

}
