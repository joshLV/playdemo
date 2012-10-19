package models.sales;

import play.db.jpa.Model;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 商品不允许发布的电子商务平台.
 * <p/>
 * User: sujie
 * Date: 5/2/12
 * Time: 10:59 AM
 */
@Entity
@Table(name = "goods_unpublished_platform")
@SolrSearchable
public class GoodsUnPublishedPlatform extends Model {

    private static final long serialVersionUID = 20611810609113062L;

    @ManyToOne(cascade = CascadeType.MERGE)
    public Goods goods;

    @Enumerated(EnumType.STRING)
    @SolrField
    public GoodsPublishedPlatformType type;

    public GoodsUnPublishedPlatform() {
    }

    public GoodsUnPublishedPlatform(Goods goods, GoodsPublishedPlatformType type) {
        this.goods = goods;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GoodsUnPublishedPlatform that = (GoodsUnPublishedPlatform) o;

        if (goods != null ? !goods.equals(that.goods) : that.goods != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (goods != null ? goods.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
