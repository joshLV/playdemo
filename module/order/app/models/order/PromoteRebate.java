package models.order;

import models.consumer.User;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    public RebateStatus status;

    /**
     * 预期返利金额
     */
    @Column(name = "rebate_amount")
    public BigDecimal rebateAmount;
    /**
     * 预期可得返利
     */
    @Transient
    public BigDecimal willGetAmount;
    /**
     * 已经获得返利
     */
    @Transient
    public BigDecimal haveGotAmount;
    /**
     * 推荐日期
     */
    public Date createdAt;
    /**
     * 返利日期
     */
    public Date rebateAt;

    /**
     * 用户来源 true：注册用户
     */
    public Boolean registerFlag = false;

    /**
     * @param promoteUser
     * @param invitedUser
     * @param order
     * @param rebateAmount
     */
    public PromoteRebate(User promoteUser, User invitedUser, Order order, BigDecimal rebateAmount, boolean registerFlag) {
        this.rebateAmount = rebateAmount;
        this.promoteUser = promoteUser;
        this.invitedUser = invitedUser;
        this.order = order;
        this.status = RebateStatus.UN_CONSUMED;
        this.createdAt = new Date();
        this.registerFlag = registerFlag;
    }

    public PromoteRebate(BigDecimal willGetAmount, BigDecimal haveGotAmount) {
        this.willGetAmount = willGetAmount;
        this.haveGotAmount = haveGotAmount;
    }

    /**
     * 取得推荐产生的返利金额
     *
     * @param user
     * @return
     */
    public static PromoteRebate getRebateAmount(User user) {
        System.out.println(user.id);
        List<PromoteRebate> promoteRebates = PromoteRebate.find("promoteUser = ?", user).fetch();
        BigDecimal willGetAmount = BigDecimal.ZERO;
        BigDecimal haveGotAmount = BigDecimal.ZERO;
        for (PromoteRebate rebate : promoteRebates) {
            if (rebate.status == RebateStatus.UN_CONSUMED)
                willGetAmount = willGetAmount.add(rebate.rebateAmount == null ? BigDecimal.ZERO : rebate.rebateAmount);
            else
                haveGotAmount = haveGotAmount.add(rebate.rebateAmount == null ? BigDecimal.ZERO : rebate.rebateAmount);
        }

        return new PromoteRebate(willGetAmount, haveGotAmount);
    }

    public static JPAExtPaginator<PromoteRebate> findAccounts(User user, PromoteRebateCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<PromoteRebate> orderPage = new JPAExtPaginator<>
                ("PromoteRebate p", "p", PromoteRebate.class, condition.getFilter(user),
                        condition.params)
                .orderBy("p.createdAt desc");
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
        return orderPage;
    }
}
