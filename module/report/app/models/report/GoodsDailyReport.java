package models.report;

import models.sales.Goods;
import models.sales.Shop;
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

    public GoodsDailyReport() {

    }

    public GoodsDailyReport(Goods goods, long buyCount, long orderCount, BigDecimal originalAmount) {
        this.goods = goods;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
    }

    public static JPAExtPaginator<GoodsDailyReport> query(ReportCondition condition, int pageNumber,
                                                         int pageSize) {
        JPAExtPaginator<GoodsDailyReport> page = new JPAExtPaginator<>("GoodsDailyReport r",
                "new GoodsDailyReport(r.goods, sum(r.buyCount), sum(r.orderCount), sum(r.originalAmount))",
                GoodsDailyReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.goods").orderBy("r.goods");
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
}
