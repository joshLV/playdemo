package models.order;

import models.accounts.AccountType;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-20
 * Time: 下午1:47
 */
@Entity
@Table(name = "cancel_unpaid_orders")
public class CancelUnpaidOrders extends Model {

    @Column(name = "order_number")
    @Unique
    public String orderNumber;
    @Column(name = "cancel_at")
    public Date cancelAt;
    @Column(name = "is_canceled")
    public boolean isCanceled;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    public AccountType userType;            //用户类型，个人/分销商
    @Column(name = "user_id")
    public long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商

    public CancelUnpaidOrders(String orderNumber, AccountType type, Long userId) {
        this.orderNumber = orderNumber;
        this.userType = type;
        this.userId = userId;
        this.cancelAt = new Date();
        this.isCanceled = true;
    }
}
