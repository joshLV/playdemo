package models.sales;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.solr.SolrField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-11
 * Time: 下午2:54
 */

@Table(name = "goods_schedule")
@Entity
public class GoodsSchedule extends Model {
    @Required
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
    @Required
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    @SolrField
    public Date expireAt;

    @Column(name = "created_at")
    public Date createdAt;

    public static void update(Long id, GoodsSchedule goodsSchedule) {
        GoodsSchedule updGoods = GoodsSchedule.findById(id);
        updGoods.effectiveAt = goodsSchedule.effectiveAt;
        updGoods.expireAt = goodsSchedule.expireAt;
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
}
