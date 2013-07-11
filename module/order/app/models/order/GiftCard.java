package models.order;

import models.sales.Goods;
import models.sales.ImportedCouponStatus;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * @author likang
 *         Date: 13-7-11
 */
@Table(name = "gift_cards")
public class GiftCard extends Model{
    @Column(name = "number")
    public String number;

    @Column(name = "password")
    public String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public ImportedCouponStatus status;

    @Column(name = "info")
    public String info; //json 格式的信息
}
