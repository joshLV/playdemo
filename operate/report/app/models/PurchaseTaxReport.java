package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

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

    @Column(name = "sale_price")
    public BigDecimal salePrice;
    
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


    public PurchaseTaxReport(Goods goods, long buyCount, BigDecimal salePrice, BigDecimal originalAmount) {
        this.supplier = goods.getSupplier();
        this.goods = goods;
        this.buyCount = buyCount;
        this.salePrice = salePrice;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }

    public PurchaseTaxReport(long buyCount, BigDecimal salePrice, BigDecimal originalAmount) {
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }    

    public static JPAExtPaginator<PurchaseTaxReport> query(PurchaseTaxReportCondition condition, int pageNumber,
                                                           int pageSize) {
        JPAExtPaginator<PurchaseTaxReport> page = new JPAExtPaginator<>("ECoupon r",
                "new PurchaseTaxReport(r.goods, count(r.id), r.salePrice, sum(r.salePrice)) ",
                PurchaseTaxReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.goods, r.salePrice").orderBy("r.goods.supplierId");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static PurchaseTaxReport summary(PurchaseTaxReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select count(r.id), (sum(r.salePrice)/count(r.id)), sum(r.salePrice) " +
                "from ECoupon r where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
         Object[] summary = (Object[]) q.getSingleResult();
        if (summary == null || summary[0] == null) {
            return new PurchaseTaxReport(0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new PurchaseTaxReport((Long) summary[0], (BigDecimal) summary[1], (BigDecimal) summary[2]);
    }
}
