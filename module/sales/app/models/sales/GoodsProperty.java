package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * User: yan
 * Date: 13-6-6
 * Time: 上午10:36
 */
@Table(name = "goods_properties")
@Entity
public class GoodsProperty extends Model {
    /**
     * 商品
     */
    @Column(name = "goods_id")
    public Long goodsId;
    /**
     * 关键字
     */
    @Column(name = "property_name")
    public String name;

    /**
     * 值
     */
    @Column(name = "property_value")
    public String value;

    public GoodsProperty(Goods goods, String name, String value) {
        this.goodsId = goods.id;
        this.name = name;
        this.value = value;
    }
}
