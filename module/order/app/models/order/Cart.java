package models.order;

import cache.CacheHelper;
import models.accounts.AccountType;
import models.consumer.User;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.SecKillGoodsItem;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "cart")
public class Cart extends Model {

    private static final long serialVersionUID = 1632320609113062L;

    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;
    @ManyToOne
    public SecKillGoodsItem secKillGoodsItem;
    public long number;

    @Column(name = "cookie_identity")
    public String cookieIdentity;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Transient
    public BigDecimal rebateValue;

    @Transient
    public BigDecimal getLineValue() {
        if (rebateValue == null) {
            rebateValue = BigDecimal.ZERO;
        }
        BigDecimal lineValue = goods.salePrice.multiply(new BigDecimal(number)).subtract(rebateValue);
        if (lineValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return lineValue;
    }

    public Cart(Goods goods, long number) {
        this.goods = goods;
        this.number = number;
    }

    public Cart(Goods goods, long number, SecKillGoodsItem item) {
        this.goods = goods;
        this.secKillGoodsItem = item;
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

    public static final String CACHE_KEY = "CART";

    private static void clearCache(User user, String cookieIdentity) {
        CacheHelper.delete(getCartCacheKey(user, cookieIdentity));
    }

    public static String getCartCacheKey(User user, String cookieIdentity) {
        return CACHE_KEY + "_U" + (user == null ? "_NULL" : user.id) + "_" + cookieIdentity;
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

        Long realStocks = goods.getRealStocks();
        //如果记录已存在，则更新记录，否则新建购物车记录
        if (cart != null) {
            // 不允许一次购买超过999
            if (cart.number + increment > 0) {
                cart.number += increment;
                cart.number = cart.number > 999 ? 999 : cart.number;
                cart.number = cart.number > realStocks ? realStocks : cart.number;
                cart.save();
                clearCache(user, cookie);
                return cart;
            } else {
                cart.delete();
                //不允许存在数量小于等于0的购物车记录
                clearCache(user, cookie);
                return null;
            }
        } else {
            if (increment <= 0) {
                return null;
            }
            increment = increment > 999 ? 999 : increment;
            increment = increment > realStocks.intValue() ? realStocks.intValue() : increment;
            if (user != null) {
                clearCache(user, cookie);
                return new Cart(user, null, goods, increment).save();
            } else {
                clearCache(user, cookie);
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

        clearCache(user, cookie);
        return query.executeUpdate();
    }

    /**
     * 取出该用户购买指定商品的数量
     *
     * @param user    用户
     * @param goodsId 商品ID
     * @return
     */
    public static long itemsNumber(User user, Long goodsId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE goods.id=:goodsId and " +
                "order.userId=:userId and order.userType=:userType and status=:status");
        q.setParameter("goodsId", goodsId);
        q.setParameter("userId", user.id);
        q.setParameter("userType", AccountType.CONSUMER);
        q.setParameter("status", OrderStatus.PAID);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
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
            return new ArrayList<>();
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder(
                "select new Cart(c.goods, SUM(c.number)) from Cart c where " +
                        "c.goods is not null and c.goods.status = :status and ( 1=2 ");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", GoodsStatus.ONSALE);

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

    /**
     * 用户所有的购物车列表
     */
    public static int findAllByGoodsId(User user, Goods goods) {
        if (user == null) {
            return 0;
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder(
                "select new Cart(c.goods, SUM(c.number)) from Cart c where " +
                        "c.goods.status = :status ");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", GoodsStatus.ONSALE);

        if (user != null) {
            sql.append(" and c.user = :user ");
            params.put("user", user);
        }
        if (goods != null) {
            sql.append("and c.goods = :goods ");
            params.put("goods", goods);
        }
        sql.append(" group by c.goods");

        Query query = play.db.jpa.JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        int count = 0;
        List<Cart> cartList = query.getResultList();
        for (Cart cart : cartList) {
            count += cart.number;
        }

        return count;
    }
}
