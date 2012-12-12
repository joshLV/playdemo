package models.consumer;


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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-12
 * Time: 下午3:52
 */
@Table(name = "user_coins")
@Entity
public class UserCoin extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    public Long coins;

    /**
     * 备注
     */
    public String remarks;

    @Column(name = "created_at")
    public Date createdAt;

    public static JPAExtPaginator<UserCoin> find(User user, UserCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<UserCoin> coinsPage = new JPAExtPaginator<>
                ("UserCoin u", "u", UserCoin.class, condition.getCondition(user),
                        condition.paramsMap)
                .orderBy("u.createdAt desc");
        coinsPage.setPageNumber(pageNumber);
        coinsPage.setPageSize(pageSize);

        return coinsPage;
    }

    public static Long coinNumber(User user, List<UserCoin> resultList) {
        Long coinNumber = 0l;
        if (resultList.size() == 0) {
            return coinNumber;
        }

        for (UserCoin userCoin : resultList) {
            coinNumber += userCoin.coins;
        }
        return coinNumber;
    }
}
