package models.order;

import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-5-30
 * Time: 下午3:32
 */
@Entity
@Table(name = "sent_coupon_message")
public class SentCouponMessage extends Model {
    @Column(name = "coupon_number")
    @Unique
    public String couponNumber;
    @Column(name = "sent_at")
    public Date sentAt;
    @Column(name = "is_send")
    public boolean isSend;
    public String mobile;
    @Column(name = "goods_name")
    public String goodsName;

    public SentCouponMessage(String eCouponSn, String mobile, String goodsName) {
        this.couponNumber = eCouponSn;
        this.isSend = true;
        this.sentAt = new Date();
        this.mobile = mobile;
        this.goodsName = goodsName;
    }
}
