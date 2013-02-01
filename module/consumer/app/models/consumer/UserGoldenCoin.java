package models.consumer;


import com.uhuila.common.util.DateUtil;
import models.sales.Goods;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-12
 * Time: 下午3:52
 */
@Table(name = "user_golden_coins")
@Entity
public class UserGoldenCoin extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "number")
    public Long number;

    /**
     * 备注
     */
    public String remarks;


    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 累计到一定天数的奖励为true
     */
    @Column(name = "is_present")
    public boolean isPresent;

    public UserGoldenCoin() {
    }

    public UserGoldenCoin(User user, Long number, Goods goods, String remarks, boolean isPresent) {
        this.number = number;
        this.user = user;
        this.goods = goods;
        this.remarks = remarks;
        this.isPresent = isPresent;
        this.createdAt = new Date();
    }

    public static JPAExtPaginator<UserGoldenCoin> find(User user, UserCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<UserGoldenCoin> coinsPage = new JPAExtPaginator<>
                ("UserGoldenCoin u", "u", UserGoldenCoin.class, condition.getCoinsCondition(user),
                        condition.paramsMap)
                .orderBy("u.createdAt desc");
        coinsPage.setPageNumber(pageNumber);
        coinsPage.setPageSize(pageSize);

        return coinsPage;
    }

    /**
     * 取得该用户所有的签到次数
     *
     * @param user
     * @return
     */
    public static Long getCheckinNumber(User user) {
        EntityManager entityManager = JPA.em();
        String sql = "SELECT count( id ) FROM UserGoldenCoin WHERE user = :user and createdAt >=:beginDate and createdAt <=:endDate";
        Query q = entityManager.createQuery(sql);

        q.setParameter("user", user);
        q.setParameter("beginDate", DateUtil.firstDayOfMonth());
        q.setParameter("endDate", DateUtil.getEndOfDay());
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }


    /**
     * 取得该用户签到的总金币数
     *
     * @param user
     * @return
     */
    public static Long getTotalCoins(User user) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( number ) FROM UserGoldenCoin WHERE user = :user ");
        q.setParameter("user", user);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    /**
     * 判断当天是否签到
     *
     * @param user
     * @return
     */
    public static UserGoldenCoin getCheckinInfo(User user, boolean isPresent) {
        return UserGoldenCoin.find("user=? and isPresent = ? and createdAt >=? and createdAt <=? ", user, isPresent, DateUtil.getBeginOfDay(), DateUtil.getEndOfDay()).first();
    }

    /**
     * 签到
     *
     * @param user
     * @param goods
     * @param remarks
     */
    public static void checkin(User user, Goods goods, String remarks) {
        UserGoldenCoin goldenCoin = UserGoldenCoin.getCheckinInfo(user, false);
        if (goldenCoin == null) {
            new UserGoldenCoin(user, +5L, goods, remarks, false).save();
        }
    }


    /**
     * 计算可以兑换抵用券的比例
     *
     * @param coinsNumber
     * @return
     */
    public static Long getPresentOfCoins(Long coinsNumber) {
        return coinsNumber / 500;
    }
}
