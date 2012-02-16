package models.order;

import models.consumer.User;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders extends Model {
    @ManyToOne
    public User user;

    @Column(name="order_no")
    public String orderNumber;

    public String status;

    public float amount;

    @Column(name="account_pay")
    public float accountPay;

    @Column(name="discount_pay")
    public float discountPay;

    @Column(name="need_pay")
    public float needPay;

    @Column(name="buyer_phone")
    public String buyerPhone;

    @Column(name="buyer_mobile")
    public String buyerMobile;

    public String remark;

    @Column(name="pay_method")
    public String payMethod;

    @Column(name="pay_request_id")
    public Long payRequestId;

    @Column(name="receiver_phone")
    public String receiverPhone;

    @Column(name="receiver_mobile")
    public String receivMobile;

    @Column(name="receiver_address")
    public String receivAddress;

    @Column(name="receiver_name")
    public String receivName;

    public String postcode;

    @Column(name="createdAt")
    public Date createdAt;

    @Column(name="updatedAt")
    public Date updatedAt;

    @Column(name="lockVersion")
    public int lockVersion;

    public int deleted;

    public Orders(User user){
        this.user = user;
        this.status = OrderStatus.UNPAID.toString();
        this.deleted = 0;
        this.orderNumber = "";


        this.amount         = 0;
        this.accountPay     = 0;
        this.needPay        = 0;
        this.discountPay    = 0;

        this.lockVersion    = 0;

        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

}
