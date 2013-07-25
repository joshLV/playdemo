package models.sales;

import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Map;

/**
 * 商户在美团或点评等团购上的商品信息
 * <p/>
 * User: yan
 * Date: 13-7-23
 * Time: 下午3:52
 */
@Entity
@Table(name = "supplier_resaler_products")
public class SupplierResalerProduct extends Model {
    @ManyToOne
    @JoinColumn(name="supplier_id")
    public Supplier supplier;

    @ManyToOne
    @JoinColumn(name="goods_id")
    public Goods goods;

    @Column(name = "partner_goods_id")
    public String partnerGoodsId;

    @Column(name = "partner_goods_name")
    public String partnerGoodsName;

    @ManyToOne
    @JoinColumn(name="resaler_id")
    public Resaler resaler;


}
