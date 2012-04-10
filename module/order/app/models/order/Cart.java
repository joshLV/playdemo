package models.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

@Entity
@Table(name = "cart")
public class Cart extends Model {
    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;

    public long number;

    @Column(name = "cookie_identity")
    public String cookieIdentity;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    public Cart(Goods goods, long number) {
        this.goods = goods;
        this.number = number;
    }

    public Cart(User user, String cookieIdentity, Goods goods, long number) {
        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods = goods;
        this.number = number;
        this.lockVersion = 0;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    /**
     * 加入或修改购物车列表
     *
     * @param user      用户
     * @param cookie    用户cookie
     * @param goods     商品
     * @param increment 购物车中商品数增量，
     *                  若购物车中无此商品，则新建条目
     *                  若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */

    public static Cart order(User user, String cookie,
                             Goods goods, int increment) {
        if ((user == null && cookie == null) || goods == null) {
            return null;
        }

        Cart cart;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        } else {
            cart = Cart.find("byCookieIdentityAndGoods", cookie, goods).first();
        }

        //如果记录已存在，则更新记录，否则新建购物车记录
        if (cart != null) {
            if (cart.number + increment > 0) {
                cart.number += increment;
                cart.save();
                return cart;
            } else {
                //不允许存在数量小于等于0的购物车记录
                return null;
            }
        } else {
            if (increment <= 0) {
                return null;
            }
            if (user != null) {
                return new Cart(user, null, goods, increment).save();
            } else {
                return new Cart(null, cookie, goods, increment).save();
            }
        }

    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param user     用户
     * @param cookie   用户cookie
     * @param goodsIds 商品列表，若未指定，则删除该用户所有的购物车条目
     * @return 成功删除的数量
     */
    public static int delete(User user, String cookie, List<Long> goodsIds) {
        if (user == null && cookie == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("delete from Cart c where (1=2");
        Map<String, Object> params = new HashMap<String, Object>();
        if (user != null) {
            sql.append(" or c.user = :user");
            params.put("user", user);
        }
        if (cookie != null) {
            sql.append(" or c.cookieIdentity = :cookie");
            params.put("cookie", cookie);
        }
        sql.append(")");
        if (goodsIds != null) {
            sql.append(" and c.goods.id in :ids");
            params.put("ids", goodsIds);
        }

        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.executeUpdate();
    }

    /**
     * 列出所有符合条件的购物车条目，合并数量后输出
     *
     * @param user   用户
     * @param cookie 用户cookie
     * @param type   商品类型
     * @return 合并数量后的购物车条目列表
     */
    public static List<Cart> findAll(User user, String cookie,
                                     MaterialType type) {
        if (user == null && cookie == null && type == null) {
            return new ArrayList<Cart>();
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder(
                "select new Cart(c.goods, SUM(c.number)) from Cart c where ( 1=2 ");
        Map<String, Object> params = new HashMap<String, Object>();

        if (user != null) {
            sql.append("or c.user = :user ");
            params.put("user", user);
        }
        if (cookie != null) {
            sql.append("or c.cookieIdentity = :cookie ");
            params.put("cookie", cookie);
        }
        sql.append(") ");
        if (type != null) {
            sql.append("and c.goods.materialType = :type ");
            params.put("type", type);
        }
        sql.append(" group by c.goods");

        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }

    /**
     * 用户所有的购物车列表
     */
    public static List<Cart> findAll(User user, String cookie) {
        return findAll(user, cookie, null);
    }
}
