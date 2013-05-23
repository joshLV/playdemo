package models.ktv;

import models.sales.Goods;
import models.sales.Shop;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * @author likang
 *
 * 商品与 KTV产品的对应关系
 *
 * shop&product 唯一
 * goods 唯一
 */
@Entity
@Table(name = "ktv_product_goods")
public class KtvProductGoods extends Model {
    @ManyToOne
    @JoinColumn(name = "shop_id")
    public Shop shop;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @ManyToOne
    @JoinColumn(name = "product_id")
    public KtvProduct product;

    public static KtvProductGoods findGoods(Shop shop, KtvProduct product) {
        return KtvProductGoods.find("shop=? and product=? ", shop, product).first();
    }
}
