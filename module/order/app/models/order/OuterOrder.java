package models.order;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-18
 */
@Entity
@Table(name = "outer_order", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"partner", "order_id"})})        //定义两对联合唯一约束
public class OuterOrder extends Model {
    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    @Column(name = "order_id", nullable = true)
    public Long orderId;       //合作伙伴的订单ID

    @Basic(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "ybq_order_id")
    public Order ybqOrder;          //一百券的订单

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public OuterOrderStatus status; //此订单的执行状态

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "message")
    public String message;          //此订单的完整信息

    @Column(name = "created_at")
    public Date createdAt;          //创建日期

    @Version
    @Column(name = "lock_version")
    public int lockVersion;         //乐观锁

    public OuterOrder() {
        this.createdAt = new Date();
        this.lockVersion = 0;
    }

    public static OuterOrder getOuterOrder(Order ybqOrder) {
        return OuterOrder.find("ybqOrder=?", ybqOrder).first();

    }

}
