package models.report;

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
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门店日报表.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 10:36 AM
 */
@Entity
@Table(name = "report_daily_shop")
public class ShopDailyReport extends Model {
    @ManyToOne
    public Supplier supplier;
    @Column(name = "created_at")
    public Date createdAt;
    @ManyToOne
    public Shop shop;
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

    public static JPAExtPaginator<ShopDailyReport> query(ReportCondition condition, int pageNumber,
                                                         int pageSize) {
        JPAExtPaginator<ShopDailyReport> page = new JPAExtPaginator<>("ShopDailyReport r", "r",
                ShopDailyReport.class, condition.getFilter(),
                condition.getParamMap()).orderBy(condition.getOrderByExpress());
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.orderCount),sum(r.originalAmount) " +
                "from ShopDailyReport r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            System.out.println("condition.getParamMap().get(" + key + "):" + condition.getParamMap().get(key));
            q.setParameter(key, condition.getParamMap().get(key));
        }
        Object summary = q.getSingleResult();
        System.out.println("summary.class:" + summary.getClass());
        return null;
    }
}
