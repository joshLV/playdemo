package models;

import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 采购税表.
 * <p/>
 * 采购对应的是电子券ECoupon已经消费的记录
 * User: sujie
 * Date: 5/30/12
 * Time: 4:08 PM
 */
@Entity
@Table(name = "report_purchase_ecoupon")
public class PurchaseECouponReport extends Model {
    @ManyToOne
    public Supplier supplier;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "sale_price")
    public BigDecimal salePrice;
    @Column(name = "face_value")
    public BigDecimal faceValue;
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


    public PurchaseECouponReport(Goods goods, long buyCount, BigDecimal faceValue, BigDecimal salePrice, BigDecimal originalAmount) {
        this.supplier = goods.getSupplier();
        this.goods = goods;
        this.buyCount = buyCount;
        this.salePrice = salePrice;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
        this.faceValue = faceValue;
    }

    public PurchaseECouponReport(long buyCount, BigDecimal salePrice, BigDecimal originalAmount) {
        this.buyCount = buyCount;
        this.originalAmount = originalAmount;
        this.tax = BigDecimal.ZERO;
        this.noTaxAmount = BigDecimal.ZERO;
    }

    public static List<PurchaseECouponReport> query(PurchaseECouponReportCondition condition, Long operatorId, Boolean hasSeeAllSupplierPermission) {
        Query query = JPA.em()
                .createQuery(
                        "select new PurchaseECouponReport(r.goods, count(r.id),r.faceValue, r.originalPrice, sum(r.originalPrice)) "
                                + " from ECoupon r where "
                                + condition.getFilter(operatorId, hasSeeAllSupplierPermission) + " group by r.goods order by r.goods.supplierId");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        return query.getResultList();
    }

    public static PurchaseECouponReport summary(List<PurchaseECouponReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new PurchaseECouponReport(0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long buyCount = 0l;
        BigDecimal salePrice = BigDecimal.ZERO;
        BigDecimal originalAmount = BigDecimal.ZERO;
        for (PurchaseECouponReport item : resultList) {
            buyCount += item.buyCount;
            salePrice = salePrice.add(item.salePrice);
            originalAmount = originalAmount.add(item.originalAmount);
        }
        return new PurchaseECouponReport(buyCount, salePrice, originalAmount);
    }
}
