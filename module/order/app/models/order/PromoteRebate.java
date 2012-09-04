package models.order;

import models.consumer.User;
import org.hibernate.annotations.Entity;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-3
 * Time: 上午10:13
 */
@Entity
@Table(name = "promote_rebate")
public class PromoteRebate extends Model {
    /**
     * 推荐人ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoter_id", nullable = true)
    public User promoteUser;

    /**
     * 受邀者ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = true)
    public User invitedUser;

    /**
     * 订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    /**
     * 推荐状态
     */
    public RebateStatus status;

    /**
     * 返利金额
     */
    @Column(name = "rebate_amount")
    public BigDecimal rebateAmount;

    /**
     * 推荐日期
     */
    public Date createdAt;
    /**
     * 返利日期
     */
    public Date rebateAt;

}
