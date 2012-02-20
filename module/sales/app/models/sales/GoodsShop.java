package models.sales;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "goods_shops")
public class GoodsShop extends Model {
    @Column(name = "goods_id")
    public long goodsId;
    @Column(name = "shop_id")
    public long shopId;
}
