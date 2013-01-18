package models.sales;

import cache.CacheHelper;
import com.uhuila.common.util.DateUtil;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-11
 * Time: 下午2:54
 */

@Table(name = "goods_schedule")
@Entity
public class GoodsSchedule extends Model {
    @ManyToOne
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    /**
     * 有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;
    /**
     * 有效结束日
     */
    @Required
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    @Column(name = "created_at")
    public Date createdAt;

    public static final String CACHEKEY = "GOODS_SCHEDULE";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    public static void update(Long id, GoodsSchedule goodsSchedule) {
        GoodsSchedule updGoods = GoodsSchedule.findById(id);
        updGoods.effectiveAt = goodsSchedule.effectiveAt;
        updGoods.expireAt = DateUtil.getEndOfDay(goodsSchedule.expireAt);
        updGoods.id = id;
        updGoods.save();
    }

    public static JPAExtPaginator<GoodsSchedule> findByCondition(GoodsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<GoodsSchedule> goodsPage = new JPAExtPaginator<>
                ("GoodsSchedule g", "g", GoodsSchedule.class, condition.getScheduleFilter(),
                        condition.getParamMap())
                .orderBy("g.createdAt desc");
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);

        return goodsPage;

    }

    /**
     * @param goods
     * @return
     */
    public static List<GoodsSchedule> findSchedule(Goods goods, Date currDate) {
        if (goods == null) {
            return GoodsSchedule.find("effectiveAt <= ? and expireAt >=?", currDate, currDate).fetch();
        }
        List<GoodsSchedule> scheduleList = GoodsSchedule.find("goods=? and effectiveAt <= ? and expireAt >=?", goods, currDate, currDate).fetch();
        return scheduleList;
    }
}
