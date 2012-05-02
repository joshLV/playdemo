package models.sales;

import play.data.validation.Required;
import play.db.jpa.Model;

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
public class GoodsUnPublishedPlatform extends Model {
    @ManyToOne(cascade = CascadeType.ALL)
    public Goods goods;

    @Enumerated(EnumType.STRING)
    public GoodsPublishedPlatformType type;

    public GoodsUnPublishedPlatform() {
    }

    public GoodsUnPublishedPlatform(Goods goods, GoodsPublishedPlatformType type) {
        this.goods = goods;
        this.type = type;
    }
}
