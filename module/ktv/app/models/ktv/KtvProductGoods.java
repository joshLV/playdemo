package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import models.sales.Shop;
import play.data.validation.Unique;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * @author likang
 *         <p/>
 *         商品与 KTV产品的对应关系
 *         <p/>
 *         shop&product 唯一
 *         goods 唯一
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

    /**
     * 0:待更新 1已更新
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "need_sync")
    public DeletedStatus needSync = DeletedStatus.UN_DELETED;

}
