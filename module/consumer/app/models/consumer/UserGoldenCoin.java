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
import javax.persistence.Transient;
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

    @Column(name = "golden_coin_number")
    public Long number;

    /**
     * 备注
     */
    public String remarks;


    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    public Long checkinTimes;

    @Transient
    public Long totalCoins;


    public static JPAExtPaginator<UserGoldenCoin> find(User user, UserCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<UserGoldenCoin> coinsPage = new JPAExtPaginator<>
                ("UserGoldenCoin u", "u", UserGoldenCoin.class, condition.getCondition(user),
                        condition.paramsMap)
                .orderBy("u.createdAt desc");
        coinsPage.setPageNumber(pageNumber);
        coinsPage.setPageSize(pageSize);

        return coinsPage;
    }

    /**
     * 取得该用户所有的签到的金币数
     *
     * @param user
     * @return
     */
    public static Long getCoinNumber(User user) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( number ) FROM UserGoldenCoin WHERE user = :user");
        q.setParameter("user", user);

        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }


    /**
     * 取得该用户签到次数和金币数
     *
     * @param user
     * @return
     */
    public static Long getTotalCoins(User user) {
        //  取得该用户签到次数
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( number ) FROM UserGoldenCoin WHERE user = :user and createdAt >=:beginDate and createdAt <=:endDate");
        q.setParameter("user", user);
        q.setParameter("beginDate", DateUtil.getMonthFirstDay());
        q.setParameter("endDate", DateUtil.getEndOfDay());

        Object result = q.getSingleResult();

        return result == null ? 0 : (Long) result;
    }

    /**
     * 签到
     *
     * @param user
     * @param goods
     * @param remarks
     */
    public static void checkin(User user, Goods goods, String remarks) {
        UserGoldenCoin goldenCoin = UserGoldenCoin.find("user=? and createdAt >=? and createdAt <=? ", user, DateUtil.getBeginOfDay(), DateUtil.getEndOfDay()).first();
        if (goldenCoin == null) {
            goldenCoin = new UserGoldenCoin();
            goldenCoin.number = 1L;
            goldenCoin.user = user;
            goldenCoin.goods = goods;
            goldenCoin.remarks = remarks;
            goldenCoin.createdAt = new Date();
            goldenCoin.save();
        }
    }
}
