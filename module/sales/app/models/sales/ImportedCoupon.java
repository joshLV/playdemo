package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-8
 */
@Entity
@Table(name = "imported_coupons")/*,
        uniqueConstraints = {
                @UniqueConstraint( columnNames = {"goods_id", "coupon"})})*/
public class ImportedCoupon extends Model {
    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Column(name = "coupon")
    public String coupon;

    @Column(name = "password")
    public String password;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public ImportedCouponStatus status;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "imported_at")
    public Date importedAt;

    public ImportedCoupon(){
        this.status = ImportedCouponStatus.UNUSED;
        this.importedAt = new Date();
        this.lockVersion = 1;
    }

    public ImportedCoupon(Goods goods, String coupon,String password){
        this();
        this.goods = goods;
        this.coupon = coupon;
        this.password=password;
    }
}
