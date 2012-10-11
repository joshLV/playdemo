package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午5:05
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "goods_history_unpublished_platform")
public class GoodsHistoryUnPublishedPlatform extends Model {
    private static final long serialVersionUID = 20611810609113062L;

    @ManyToOne(cascade = CascadeType.ALL)
    public GoodsHistory goodsHistory;

    @Enumerated(EnumType.STRING)
    public GoodsPublishedPlatformType type;

    public GoodsHistoryUnPublishedPlatform() {
    }

    public GoodsHistoryUnPublishedPlatform(GoodsHistory goodsHistory, GoodsPublishedPlatformType type) {
        this.goodsHistory = goodsHistory;
        this.type = type;
    }
}
