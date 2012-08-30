package models.sales;

import cache.CacheHelper;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p/>
 * User: yanjy Date: 12-7-4 Time: 上午9:56
 */
@Entity
@Table(name = "goods_statistics")
public class GoodsStatistics extends Model {
    private static final long serialVersionUID = 7063132063912120652L;

    public Long goodsId;
    /**
     * 浏览指数
     */
    @Column(name = "visitor_count")
    public Integer visitorCount;
    /**
     * 购买指数
     */
    @Column(name = "buy_count")
    public Integer buyCount;
    /**
     * 喜欢指数
     */
    @Column(name = "like_count")
    public Integer likeCount;
    /**
     * 加入购物车指数
     */
    @Column(name = "cart_count")
    public Integer cartCount;
    /**
     * 总指数
     */
    @Column(name = "summary_count")
    public Long summaryCount;

    @Transient
    public GoodsStatisticsType statisticsType;

    public GoodsStatistics(Long goodsId) {
        this.goodsId = goodsId;
        this.visitorCount = 0;
        this.buyCount = 0;
        this.likeCount = 0;
        this.cartCount = 0;
        this.summaryCount = 0l;
    }

    public static final String CACHEKEY = "GOODSSTATISTICS";

    public static final String CACHEKEY_GOODSID = "GOODSSTATISTICS_GOODSID";

    /**
     * 为避免影响秒杀活动，先关闭记录商品统计信息
     */
    public static final boolean RECORD_STATISTICS = false;

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goodsId);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goodsId);
        super._delete();
    }

    public static void addCartCount(Long goodsId) {
        if (RECORD_STATISTICS) {
            GoodsStatistics statistics = GoodsStatistics.find("goodsId",
                    goodsId).first();
            if (statistics == null) {
                statistics = new GoodsStatistics(goodsId);
            }
            statistics.cartCount++;
            statistics.likeCount++;
            updateSummaryCount(statistics);
            statistics.save();
        }
    }

    public static void addLikeCount(Long goodsId) {
        if (RECORD_STATISTICS) {
            GoodsStatistics statistics = GoodsStatistics.find("goodsId",
                    goodsId).first();
            if (statistics == null) {
                statistics = new GoodsStatistics(goodsId);
            }
            statistics.likeCount++;
            updateSummaryCount(statistics);
            statistics.save();
        }
    }

    public static void addBuyCount(Long goodsId) {
        if (RECORD_STATISTICS) {
            GoodsStatistics statistics = GoodsStatistics.find("goodsId",
                    goodsId).first();
            if (statistics == null) {
                statistics = new GoodsStatistics(goodsId);
            }
            statistics.buyCount++;
            updateSummaryCount(statistics);
            statistics.save();
        }
    }

    public static void addVisitorCount(Long goodsId) {
        if (RECORD_STATISTICS) {
            GoodsStatistics statistics = GoodsStatistics.find("goodsId",
                    goodsId).first();
            if (statistics == null) {
                statistics = new GoodsStatistics(goodsId);
            }
            statistics.visitorCount++;
            updateSummaryCount(statistics);
            statistics.save();
        }
    }

    private static void updateSummaryCount(GoodsStatistics statistics) {
        Integer summary = statistics.visitorCount + statistics.likeCount * 3
                + statistics.cartCount * 7 + statistics.buyCount * 13;
        statistics.summaryCount = Long.parseLong(String.valueOf(summary));
    }

}
