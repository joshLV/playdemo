package models.report;

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
 * TODO.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 3:25 PM
 */
@Entity
@Table(name = "report_daily_total")
public class TotalDailyReport extends Model {
    @ManyToOne
    public Supplier supplier;
    @Column(name="created_at")
    public Date createdAt;
    @Column(name="buy_count")
    public long buyCount;
    @Column(name="order_count")
    public long orderCount;
    @Column(name = "sale_amount")
    public BigDecimal saleAmount;
    @Column(name = "resale_amount")
    public BigDecimal resaleAmount;
    @Column(name = "original_amount")
    public BigDecimal originalAmount;

    public static JPAExtPaginator<TotalDailyReport> query(ReportCondition condition, int pageNumber,
                                                          int pageSize) {
        JPAExtPaginator<TotalDailyReport> page = new JPAExtPaginator<>("TotalDailyReport r","r",
                TotalDailyReport.class, condition.getFilter(),
                condition.getParamMap()).orderBy("r.createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.orderCount),sum(r.originalAmount) " +
                "from TotalDailyReport r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        Object[] summary = (Object[]) q.getSingleResult();
        if (summary == null || summary[0] == null) {
            return new ReportSummary(0, 0, BigDecimal.ZERO);
        }
        return new ReportSummary((Long) summary[0], (Long) summary[1], (BigDecimal) summary[2]);
    }
}
