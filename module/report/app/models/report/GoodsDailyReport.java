package models.report;

import models.sales.Goods;
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
    @Column(name="created_at")
    public Date createdAt;
    @ManyToOne
    public Goods goods;
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

    public static JPAExtPaginator<GoodsDailyReport> query(ReportCondition condition, int pageNumber, int pageSize) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static ReportSummary summary(ReportCondition condition) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
