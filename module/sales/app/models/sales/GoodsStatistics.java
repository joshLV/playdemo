package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;


/**
 * <p/>
 * User: yanjy
 * Date: 12-7-4
 * Time: 上午9:56
 */
@Entity
@Table(name = "goods_statistics")
public class GoodsStatistics extends Model {

    public static Integer number = 1;
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

    public static void addCartCount(Long goodsId) {
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsId).first();
        if (statistics == null) {
            statistics = new GoodsStatistics(goodsId);
        }
        statistics.cartCount++;
        statistics.likeCount++;
        statistics.save();

    }

    public static void addLikeCount(Long goodsId) {
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsId).first();
        if (statistics == null) {
            statistics = new GoodsStatistics(goodsId);
        }
        statistics.likeCount++;
        statistics.save();

    }

    public static void addBuyCount(Long goodsId) {
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsId).first();
        if (statistics == null) {
            statistics = new GoodsStatistics(goodsId);
        }
        statistics.buyCount++;
        statistics.save();

    }

    public static void addVisitorCount(Long goodsId) {
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsId).first();
        if (statistics == null) {
            statistics = new GoodsStatistics(goodsId);
        }
        statistics.visitorCount++;
        statistics.save();

    }

    public static GoodsStatistics addSummaryCount(Long goodsId) {
        GoodsStatistics statistics = GoodsStatistics.find("goodsId", goodsId).first();
        Integer summary = statistics.visitorCount+ statistics.likeCount  + statistics.cartCount+ statistics.buyCount ;
        statistics.summaryCount = Long.parseLong(String.valueOf(summary));
        statistics.save();
        return statistics;
    }

}
