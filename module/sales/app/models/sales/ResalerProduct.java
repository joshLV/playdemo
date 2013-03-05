package models.sales;

import com.google.gson.Gson;
import com.uhuila.common.constants.DeletedStatus;
import models.order.OuterOrderPartner;
import org.hibernate.annotations.Index;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 发布到第三方产品的信息
 *
 * @author likang
 * Date: 13-1-8
 */
@Entity
@Table(name = "resaler_product")
public class ResalerProduct extends Model {
    private static Long BASE_LINK_ID = 10000L;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;       //合作伙伴

    @ManyToOne
    public Goods goods;                     //关联的一百券商品

    @Column(name = "goods_link_id")
    @Index(name = "goods_link_id_index")
    public Long goodsLinkId;                //商品link_id

    @Column(name = "partner_product_id")
    public String partnerProductId;         //第三方的产品ID

    @Column(name = "url")
    public String url;                      //第三方的url

    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    @Column(name = "creator_id")
    public Long creatorId;                  //创建人ID

    @Transient
    public String creator;

    @Column(name = "last_modifier_id")
    public Long lastModifierId;             //最后修改人ID

    @Transient
    public String lastModifier;

    @Column(name = "created_at")
    public Date createdAt;                  //创建时间

    @Column(name = "updated_at")
    public Date updatedAt;                  //最后修改时间

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public ResalerProductStatus status;     //状态

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;           //是否删除，0 未删除， 1 已删除

    @Column(name = "end_sale")
    public Date endSale;                    //下架时间

    public ResalerProduct() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = ResalerProductStatus.STAGING;
        this.deleted = DeletedStatus.UN_DELETED;
    }

    public static ResalerProduct generate(OuterOrderPartner partner, Goods goods) {
        ResalerProduct product = new ResalerProduct().save();
        product.partner = partner;
        product.goods = goods;
        product.goodsLinkId = product.id + 20000;
        return product.save();
    }

    public static ResalerProduct alloc(OuterOrderPartner partner, Goods goods) {
        ResalerProduct product = ResalerProduct.find("byPartnerAndGoodsAndStatus",
                partner, goods, ResalerProductStatus.STAGING).first();
        if (product == null) {
            product = generate(partner, goods);
        }
        return product;
    }

    public static Goods getGoods(Long  goodsLinkId, OuterOrderPartner partner) {
        ResalerProduct product = ResalerProduct.find("byGoodsLinkIdAndPartner", goodsLinkId, partner).first();
        if (product == null && goodsLinkId < BASE_LINK_ID) {
            return Goods.findById(goodsLinkId);
        }
        return product == null ? null : product.goods;
    }

    public ResalerProduct goods(Goods goods) {
        this.goods = goods;
        return this;
    }

    public ResalerProduct creator(long creatorId) {
        this.creatorId = creatorId;
        this.lastModifierId = creatorId;
        return this;
    }

    public ResalerProduct lastModifier(Long lastModifierId) {
        this.lastModifierId = lastModifierId;
        return this;
    }

    public ResalerProduct partnerProduct(String partnerProductId) {
        this.partnerProductId = partnerProductId;
        switch (partner) {
            case JD:
                this.url = "http://tuan.360buy.com/team-" + partnerProductId + ".html"; break;
            case DD:
                this.url = "http://tuan.dangdang.com/product.php?product_id=" + partnerProductId; break;
            case TB:
                this.url = "http://item.taobao.com/item.htm?id=" + partnerProductId; break;
            case YHD:
                this.url = "http://www.1mall.com/item/" + partnerProductId; break;
            case WB:
                this.url = "http://t.58.com/sh/" + partnerProductId + "/"; break;
            default:
                break;
        }
        return this;
    }

    public ResalerProduct url(String url) {
        this.url = url;
        return this;
    }

    public ResalerProduct status(ResalerProductStatus status) {
        this.status = status;
        return this;
    }
}
