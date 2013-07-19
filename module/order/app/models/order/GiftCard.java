package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import models.sales.ImportedCouponStatus;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "applied_at")
    public Date appliedAt;//来预约的日期

    @Column(name = "processed_at")
    public Date proccessedAt;//处理的日期

    public GiftCard() {
        createdAt = new Date();
        disabled = DeletedStatus.UN_DELETED;
        status = ImportedCouponStatus.UNUSED;
    }

    public static JPAExtPaginator<GiftCard> findByCondition(GiftCardCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<GiftCard> page = new JPAExtPaginator<>(null, null, GiftCard.class, condition.getFilter(),
                condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
