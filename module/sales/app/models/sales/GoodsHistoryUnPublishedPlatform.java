package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午5:05
 */

@Entity
@Table(name = "goods_history_unpublished_platform")
public class GoodsHistoryUnPublishedPlatform extends Model {
    private static final long serialVersionUID = 20611810609113062L;

    @ManyToOne(cascade = CascadeType.ALL)
    public GoodsHistory goodshistory;

    @Enumerated(EnumType.STRING)
    public GoodsPublishedPlatformType type;

    public GoodsHistoryUnPublishedPlatform() {
    }

    public GoodsHistoryUnPublishedPlatform(GoodsHistory goodsHistory, GoodsPublishedPlatformType type) {
        this.goodshistory = goodsHistory;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GoodsHistoryUnPublishedPlatform that = (GoodsHistoryUnPublishedPlatform) o;

        if (goodshistory != null ? !goodshistory.equals(that.goodshistory) : that.goodshistory != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (goodshistory != null ? goodshistory.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
