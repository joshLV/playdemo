package models.sales;

import cache.CacheHelper;
import models.consumer.User;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-29
 * Time: 下午3:26
 */
@Table(name = "browsed_goods")
@Entity
public class BrowsedGoods extends Model {
    private static final long serialVersionUID = 9063231063912530652L;
    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;
    @Column(name = "cookie_identity")
    public String cookieIdentity;
    /**
     * 浏览指数
     */
    @Column(name = "visitor_count")
    public Integer visitorCount = 0;

    @Column(name = "updated_at")
    public Date updatedAt;
    public static final String CACHEKEY = "BROSWEDGOODS";

    public static final String CACHEKEY_GOODSID = "BROSWEDGOODS_GOODSID";

    /**
     * 为避免影响秒杀活动，先关闭记录商品统计信息
     */
    public static final boolean RECORD_STATISTICS = false;

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goods.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goods.id);
        super._delete();
    }

    public BrowsedGoods(Goods goods, long visitorCount) {
        this.goods = goods;
        this.visitorCount = (int)visitorCount;
        this.updatedAt = new Date();
    }

    public BrowsedGoods(User user, String cookieIdentity, Goods goods) {
        this(user, cookieIdentity, goods, 1);
    }

    public BrowsedGoods(User user, String cookieIdentity, Goods goods, Integer visitorCount) {

        this.user = user;
        this.cookieIdentity = cookieIdentity;
        this.goods = goods;
        this.visitorCount = visitorCount;
        this.updatedAt = new Date();
    }

    /**
     * 记录用户浏览过的商品
     *
     * @param user
     * @param cookie
     * @param goods
     */
    public static void addVisitorCount(User user, String cookie,
                                       Goods goods) {
        if ((user == null && cookie == null) || goods == null) {
            return;
        }
        BrowsedGoods browsedGoods;

        if (user != null) {
            browsedGoods = BrowsedGoods.find("byUserAndGoods", user, goods).first();
        } else {
            browsedGoods = BrowsedGoods.find("byCookieIdentityAndGoods", cookie, goods).first();
        }
        if (browsedGoods != null) {
            browsedGoods.visitorCount++;
            browsedGoods.updatedAt = new Date();
            browsedGoods.save();
        } else {
            new BrowsedGoods(user, cookie, goods).save();
        }

    }

    /**
     * 查询浏览的n个商品
     *
     * @param user
     * @param cookieValue
     * @param limit
     * @return
     */
    public static List<BrowsedGoods> find(User user, String cookieValue, int limit) {
        if ((user == null && cookieValue == null)) {
            return new ArrayList<>();
        }
        //构建查询条件
        StringBuilder sql = new StringBuilder(
                "select b from BrowsedGoods b where " +
                        "b.goods is not null and b.goods.status = :status and ( 1=2 ");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", GoodsStatus.ONSALE);

        if (user != null) {
            sql.append("or b.user = :user ");
            params.put("user", user);
        }
        if (cookieValue != null) {
            sql.append("or b.cookieIdentity = :cookie ");
            params.put("cookie", cookieValue);
        }
        sql.append(") ");

        sql.append(" group by b.goods order by updatedAt desc,b.visitorCount desc");

        Query query = JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        query.setMaxResults(limit);
        return query.getResultList();

    }

    /**
     * 首页”猜你喜欢“改为显示：最近n天所有用户浏览量最多的n个商品
     *
     * @param limit
     * @return
     */
    public static List<BrowsedGoods> findTop(int limit, int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - days);
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select new models.sales.BrowsedGoods(b.goods,sum(b.visitorCount) ) from BrowsedGoods b where b.updatedAt >= :dateLimit group by b.goods order by sum(b.visitorCount) desc");
        q.setParameter("dateLimit", cal.getTime());
        q.setMaxResults(limit);
        return q.getResultList();
    }
}

