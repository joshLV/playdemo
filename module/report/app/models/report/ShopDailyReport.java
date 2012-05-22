package models.report;

import models.sales.Shop;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
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
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Column(name = "created_at")
    public Date createdAt;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    public Shop shop;

    @Column(name = "buy_count")
    public Long buyCount;

    @Column(name = "order_count")
    public Long orderCount;

    @Column(name = "sale_amount")
    public BigDecimal saleAmount;

    @Column(name = "resale_amount")
    public BigDecimal resaleAmount;

    @Column(name = "original_amount")
    public BigDecimal originalAmount;

    public ShopDailyReport() {

    }

    public ShopDailyReport(Shop shop, long buyCount, long orderCount, BigDecimal originalAmount) {
        this.shop = shop;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
    }

    public static JPAExtPaginator<ShopDailyReport> query(ReportCondition condition, int pageNumber,
                                                         int pageSize) {
        JPAExtPaginator<ShopDailyReport> page = new JPAExtPaginator<>("ShopDailyReport r",
                "new ShopDailyReport(r.shop, sum(r.buyCount), sum(r.orderCount), sum(r.originalAmount))",
                ShopDailyReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.shop").orderBy("r.shop");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.orderCount),sum(r.originalAmount) " +
                "from ShopDailyReport r where " + condition.getFilter());
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
