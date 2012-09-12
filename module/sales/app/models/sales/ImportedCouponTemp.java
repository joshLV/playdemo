package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-11
 */
@Entity
@Table(name = "imported_coupon_temp")
public class ImportedCouponTemp extends Model {
    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Column(name = "coupon")
    public String coupon;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public ImportedCouponStatus status;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "imported_at")
    public Date importedAt;

    public ImportedCouponTemp(){
        this.status = ImportedCouponStatus.UNUSED;
        this.importedAt = new Date();
        this.lockVersion = 1;
    }

    public ImportedCouponTemp(Goods goods, String coupon){
        this();
        this.goods = goods;
        this.coupon = coupon;
    }
}
