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
 * 日报表.
 * <p/>
 * User: sujie
 * Date: 5/16/12
 * Time: 6:00 PM
 */
@Entity
@Table(name = "report_daily_detail")
public class DetailDailyReport extends Model {
    @ManyToOne
    public Supplier supplier;
    @Column(name="created_at")
    public Date createdAt;
    @ManyToOne
    public Shop shop;
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



    public DetailDailyReport(Shop shop, Goods goods,long buyCount, long orderCount, BigDecimal originalAmount) {
        this.shop = shop;
        this.goods = goods;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
    }

    public static JPAExtPaginator<DetailDailyReport> query(ReportCondition condition, int pageNumber,
                                                         int pageSize) {
        JPAExtPaginator<DetailDailyReport> page = new JPAExtPaginator<>("DetailDailyReport r",
                "new DetailDailyReport(r.shop,r.goods, sum(r.buyCount), sum(r.orderCount), sum(r.originalAmount))",
                DetailDailyReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.shop,r.goods").orderBy("r.shop,r.goods");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static ReportSummary summary(ReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(r.buyCount),sum(r.orderCount),sum(r.originalAmount) " +
                "from DetailDailyReport r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        Object[] summary = (Object[]) q.getSingleResult();
        if (summary == null || summary[0] == null) {
            return new ReportSummary(0, 0, BigDecimal.ZERO);
        }
        return new ReportSummary((Long) summary[0], (Long) summary[1], (BigDecimal) summary[2]);
    }}
