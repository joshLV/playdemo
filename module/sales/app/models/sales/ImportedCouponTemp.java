package models.sales;

import com.uhuila.common.util.RandomNumberUtil;
import models.accounts.AccountType;
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

    public ImportedCouponTemp() {
        this.status = ImportedCouponStatus.UNUSED;
        this.importedAt = new Date();
        this.lockVersion = 1;
    }

    public ImportedCouponTemp(Goods goods, String coupon) {
        this(goods, coupon, "");
    }

    public ImportedCouponTemp(Goods goods, String coupon, String password) {
        this();
        this.goods = goods;
        this.coupon = coupon;
        this.password = password;
    }

    /**
     * 生成消费者唯一的券号.
     */
    private String generateAvailableEcouponSn(int length) {
        String randomNumber;
        do {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing.
            }
            randomNumber = RandomNumberUtil.generateSerialNumber(length);
        } while (isNotUniqueEcouponSn(randomNumber));
        return randomNumber;
    }

    private boolean isNotUniqueEcouponSn(String randomNumber) {
        return ImportedCoupon.find("from ImportedCoupon where coupon=?", randomNumber).fetch().size() > 0;
    }

}
