package models.order;

import com.uhuila.common.util.DateUtil;
import models.consumer.User;
import play.db.jpa.JPA;
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
     * 部分返利金额
     */
    @Column(name = "part_amount")
    public BigDecimal partAmount = BigDecimal.ZERO;

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
     * 推荐次数
     */
    @Transient
    public long promoteTimes;

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

    public PromoteRebate(User promoteUser, BigDecimal rebateAmount, long promoteTimes) {
        this.rebateAmount = rebateAmount;
        this.promoteUser = promoteUser;
        this.promoteTimes = promoteTimes;

    }

    /**
     * 取得推荐产生的返利金额
     *
     * @param user
     * @return
     */
    public static PromoteRebate getRebateAmount(User user) {
        List<PromoteRebate> promoteRebates = PromoteRebate.find("promoteUser = ?", user).fetch();
        BigDecimal willGetAmount = BigDecimal.ZERO;
        BigDecimal haveGotAmount = BigDecimal.ZERO;
        for (PromoteRebate rebate : promoteRebates) {
            if (rebate.status == RebateStatus.UN_CONSUMED) {
                willGetAmount = willGetAmount.add(rebate.rebateAmount == null ? BigDecimal.ZERO : rebate.rebateAmount);
            } else if (rebate.status == RebateStatus.PART_REBATE) {
                willGetAmount = willGetAmount.add(rebate.rebateAmount == null ? BigDecimal.ZERO : rebate.rebateAmount.subtract(rebate.partAmount));
                haveGotAmount = haveGotAmount.add(rebate.partAmount == null ? BigDecimal.ZERO : rebate.partAmount);
            } else if (rebate.status == RebateStatus.ALREADY_REBATE)
                haveGotAmount = haveGotAmount.add(rebate.rebateAmount == null ? BigDecimal.ZERO : rebate.rebateAmount);
        }

        return new PromoteRebate(willGetAmount, haveGotAmount);
    }

    /**
     * 取得返利明细
     *
     * @param user
     * @param condition
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static JPAExtPaginator<PromoteRebate> findAccounts(User user, PromoteRebateCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<PromoteRebate> orderPage = new JPAExtPaginator<>
                ("PromoteRebate p", "p", PromoteRebate.class, condition.getFilter(user),
                        condition.params)
                .orderBy("p.createdAt desc");
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);

        return orderPage;
    }

    /**
     * 取得排名的记录
     *
     * @return
     */
    public static List<PromoteRebate> findRank(PromoteRebateCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.order.PromoteRebate(p.promoteUser,sum(e.promoterRebateValue),count(distinct p.id)) "
                                + " from PromoteRebate p,ECoupon e " + condition.getFilter() +
                                " group by p.promoteUser.id order by sum(e.promoterRebateValue) desc");
        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }
        List<PromoteRebate> rankList = query.getResultList();
        return rankList;
    }

    /**
     * 取得排名的记录
     *
     * @return
     */
    public static List<PromoteRebate> findRank() {
        Query query = JPA.em()
                .createQuery(
                        "select new models.order.PromoteRebate(p.promoteUser,sum(e.promoterRebateValue),count(distinct p.id)) "
                                + " from PromoteRebate p,ECoupon e where p.order=e.order and e.status=:e_status " +
                                "and e.consumedAt>=:consumedBeginAt and e.consumedAt<=:consumedEndAt" +
                                " and (p.status=:status or p.status=:status1)  " +
                                " group by p.promoteUser.id order by sum(e.promoterRebateValue) desc");
        query.setParameter("e_status", ECouponStatus.CONSUMED);
        query.setParameter("consumedBeginAt", DateUtil.firstDayOfMonth());
        query.setParameter("consumedEndAt", DateUtil.lastDayOfMonth(new Date()));
        query.setParameter("status", RebateStatus.ALREADY_REBATE);
        query.setParameter("status1", RebateStatus.PART_REBATE);

        List<PromoteRebate> rankList = query.getResultList();


        return rankList;
    }

    public static PromoteRebate rank(User user, List<PromoteRebate> resultList) {
        if (resultList.size() == 0) {
            return null;
        }
        PromoteRebate p = null;
        for (PromoteRebate promoteRebate : resultList) {
            if (promoteRebate.promoteUser == user) {
                p = promoteRebate;
                break;
            }
        }
        return p;
    }

    public static PromoteRebate allRank(List<PromoteRebate> resultList) {
        if (resultList.size() == 0) {
            return null;
        }
        Long times = 0l;
        BigDecimal rebateAmount = BigDecimal.ZERO;

        for (PromoteRebate promoteRebate : resultList) {
            rebateAmount = rebateAmount.add(promoteRebate.rebateAmount == null ? BigDecimal.ZERO : promoteRebate.rebateAmount);
            times += promoteRebate.promoteTimes;
        }
        return new PromoteRebate(null, rebateAmount, times);
    }

    /**
     * 得到部分隐藏的用户名
     *
     * @return
     */
    public String getMaskedLoginName() {
        StringBuilder sn = new StringBuilder();
        String loginName = this.invitedUser.loginName;
        int len = loginName.length();
        if (len > 5) {
            sn.append(loginName.substring(0, 1));
            for (int i = 0; i < 3; i++) {
                sn.append("*");
            }
            sn.append(loginName.substring(4));
        }
        return sn.toString();
    }
}
