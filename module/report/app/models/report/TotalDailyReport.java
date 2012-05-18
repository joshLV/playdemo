package models.report;

import models.supplier.Supplier;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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

    public static JPAExtPaginator<TotalDailyReport> query(ReportCondition condition, int pageNumber, int pageSize) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static ReportSummary summary(ReportCondition condition) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
