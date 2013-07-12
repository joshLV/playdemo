package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import models.sales.ImportedCouponStatus;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * @author likang
 *         Date: 13-7-11
 */
@Entity
@Table(name = "gift_cards")
public class GiftCard extends Model{
    @Column(name = "coupon")
    public String coupon;

    @Column(name = "password")
    public String password;

    @Column(name = "number")
    public String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public ImportedCouponStatus status;

    @Column(name = "disabled")
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus disabled;

    @Column(name = "user_input")
    public String userInput; //json 格式

    @Column(name = "supplier_input")
    public String supplierInput; //json 格式

    public GiftCard() {
        disabled = DeletedStatus.UN_DELETED;
        status = ImportedCouponStatus.UNUSED;
    }
}
