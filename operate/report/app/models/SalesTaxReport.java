package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

/**
 * 销售税表.
 * <p/>
 * User: sujie
 * Date: 5/31/12
 * Time: 10:44 AM
 */
@Entity
@Table(name="sales_tax_reports")
public class SalesTaxReport extends Model {
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
    
    public BigDecimal salePrice;

    @Column(name = "original_amount")
    public BigDecimal originalAmount;

    public BigDecimal tax;

    @Column(name = "no_tax_amount")
    public BigDecimal noTaxAmount;

    public SalesTaxReport(Supplier supplier, Goods goods, BigDecimal salePrice,
                          long buyCount, long orderCount, BigDecimal originalAmount,
                          BigDecimal tax, BigDecimal noTaxAmount) {
        this.supplier = supplier;
        this.goods = goods;
        this.salePrice = salePrice;
        this.buyCount = buyCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
        this.tax = tax;
        this.noTaxAmount = noTaxAmount;
    }
    
    
    public SalesTaxReport(Goods goods, BigDecimal salePrice,
                          long buyCount, BigDecimal originalAmount) {
        this.supplier = goods.getSupplier();
        this.goods = goods;
        this.salePrice = salePrice;
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }    

    public SalesTaxReport(long buyCount, BigDecimal originalAmount) {
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }        

    public static JPAExtPaginator<SalesTaxReport> query(SalesTaxReportCondition condition, int pageNumber,
                                                        int pageSize) {
        JPAExtPaginator<SalesTaxReport> page = new JPAExtPaginator<>("OrderItems r, Supplier s ",
                "new SalesTaxReport(r.goods, r.salePrice, sum(r.buyNumber), sum(r.salePrice*r.buyNumber))",
                SalesTaxReport.class, condition.getFilter(),
                condition.getParamMap()).groupBy("r.goods, r.salePrice")
                .orderBy("r.goods");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    public static SalesTaxReport summary(SalesTaxReportCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select new SalesTaxReport(sum(r.buyNumber), sum(r.salePrice*r.buyNumber)) " +
                "from OrderItems r, Supplier s where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        return (SalesTaxReport)q.getSingleResult();
    }

    /**
     * 平均单价
     */
    @Transient
    public BigDecimal getPrice() {
        return originalAmount.divide(new BigDecimal(buyCount));
    }
}
