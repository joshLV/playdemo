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
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 商品日报表.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 10:37 AM
 */
@Entity
@Table(name = "report_daily_goods")
public class GoodsDailyReport extends Model {
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
    @Column(name = "sale_amount")
    public BigDecimal saleAmount;
    @Column(name = "resale_amount")
    public BigDecimal resaleAmount;
    @Column(name = "original_amount")
    public BigDecimal originalAmount;
    @Column(name = "tax_amount")
    public BigDecimal taxAmount;
    @Column(name = "no_tax_amount")
    public BigDecimal noTaxAmount;

    public GoodsDailyReport() {

    }

    public GoodsDailyReport(Supplier supplier, Goods goods, long buyCount, long orderCount, BigDecimal originalAmount) {
        this.supplier = supplier;
        this.goods = goods;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
    }

    public static JPAExtPaginator<GoodsDailyReport> query(ReportCondition condition, int pageNumber,
                                                          int pageSize) {
        JPAExtPaginator<GoodsDailyReport> page = new JPAExtPaginator<>("GoodsDailyReport r",
                "new GoodsDailyReport(r.supplier,r.goods, sum(r.buyCount), sum(r.orderCount), sum(r.originalAmount))",
                GoodsDailyReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.supplier,r.goods").orderBy("r.supplier,r.goods");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.orderCount),sum(r.originalAmount) " +
                "from GoodsDailyReport r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        Object[] summary = (Object[]) q.getSingleResult();
        if (summary == null || summary[0] == null) {
            return new ReportSummary(0, 0, BigDecimal.ZERO);
        }
        return new ReportSummary((Long) summary[0], (Long) summary[1], (BigDecimal) summary[2]);
    }

    /**
     * 平均单价
     */
    @Transient
    public BigDecimal getPrice() {
        System.out.println("originalAmount:" + originalAmount);
        System.out.println("buyCount:" + buyCount);
        if (buyCount <= 0) {
            return null;
        }
        return originalAmount.divide(new BigDecimal(buyCount), 2, RoundingMode.HALF_EVEN);
    }

}
