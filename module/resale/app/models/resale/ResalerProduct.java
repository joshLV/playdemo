package models.resale;

import models.order.OuterOrderPartner;
import models.sales.Goods;
import org.hibernate.annotations.Index;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 13-1-8
 */
@Entity
@Table(name = "resaler_product")
public class ResalerProduct extends Model {
    private static Long BASE_LINK_ID = 10000L;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    @Index(name = "partner_index")
    public OuterOrderPartner partner;      //合作伙伴

    @ManyToOne
    public Goods goods;

    @Column(name = "partner_product_id")
    public Long partnerProductId;//第三方的产品ID

    @Column(name = "url")
    public String url;          //第三方的url

    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    @Column(name = "creator_id")
    public Long creatorId;

    @Transient
    public String creator;

    @Column(name = "last_modifier_id")
    public Long lastModifierId;

    @Transient
    public String lastModifier;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Lob
    @Column(name = "latest_json_data")
    public String latestJsonData;

    /**
     * 第三方状态.
     */
    @Column(name = "outer_status")
    public String outerStatus;

    public ResalerProduct() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.partnerProductId = 0L;
    }

    public static ResalerProduct generate(Long creatorId, OuterOrderPartner partner, Goods goods) {
        ResalerProduct product = new ResalerProduct();
        product.partner = partner;
        product.goods = goods;
        product.creatorId = creatorId;
        product.lastModifierId = creatorId;
        return product.save();
    }

    public static Goods getGoods(Long  productId) {
        if (productId <= BASE_LINK_ID) {
            return Goods.findById(productId);
        }
        ResalerProduct product = ResalerProduct.findById(productId);
        return product == null ? null : product.goods;
    }

    public ResalerProduct goods(Goods goods) {
        this.goods = goods;
        return this;
    }

    public ResalerProduct lastModifier(Long lastModifierId) {
        this.lastModifierId = lastModifierId;
        return this;
    }

    public ResalerProduct partnerProduct(Long partnerProductId) {
        this.partnerProductId = partnerProductId;
        return this;
    }

    public ResalerProduct latestJson(String latestJsonData) {
        this.latestJsonData = latestJsonData;
        return this;
    }
}
