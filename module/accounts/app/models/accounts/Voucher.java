package models.accounts;


import models.accounts.Account;
import models.order.Order;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 * Date: 12-12-12
 */
@Entity
@Table(name = "vouchers")
public class Voucher extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true)
    public Account account;

    @Column(name = "value")
    public BigDecimal value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @Version
    @Column(name="lock_version")
    public int lockVersion;

    @Column(name = "name")
    public String name;

    /**
     * 创建者ID，为运营后台登录ID.
     */
    @Column(name = "operator_id")
    public Long operatorId;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 分配时间
     */
    @Column(name = "assigned_at")
    public Date assignedAt;

    /**
     * 使用时间
     */
    @Column(name = "used_at")
    public Date usedAt;

    /**
     * 过期时间
     */
    @Column(name = "expired_at")
    public Date expiredAt;
}
