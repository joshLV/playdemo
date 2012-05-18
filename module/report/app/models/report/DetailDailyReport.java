package models.report;

import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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
}
