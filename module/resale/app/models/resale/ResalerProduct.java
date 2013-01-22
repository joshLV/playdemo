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

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    @Index(name = "partner_index")
    public OuterOrderPartner partner;      //合作伙伴

    @ManyToOne
    public Goods goods;

    @Column(name = "goods_link_id")
    public Long goodsLinkId;//记录推送的linkId

    @Column(name = "partner_product_id")
    public Long partnerProductId;//第三方的产品ID

    @Column(name = "url")
    public String url;          //第三方的url

    @Column(name = "lock_version")
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

    public static ResalerProduct createProduct(OuterOrderPartner outerOrderPartner, long partnerProductId,
                                               Long creatorId, Goods goods, Long linkId) {
        ResalerProduct product = new ResalerProduct();
        product.partner = outerOrderPartner;
        product.partnerProductId =  partnerProductId;
        product.creatorId = creatorId;
        product.goods = goods;
        product.goodsLinkId = linkId;
        product.lastModifierId = creatorId;
        return product.save();
    }
}
