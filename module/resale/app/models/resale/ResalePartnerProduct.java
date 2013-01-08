package models.resale;

import models.admin.OperateUser;
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
@Table(name = "resale_partner_product")
public class ResalePartnerProduct extends Model {


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

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "creator_id")
    public Long creatorId;

    @Column(name = "last_modifier_id")
    public Long lastModifierId;

    /**
     * 第三方状态.
     */
    @Column(name = "outer_status")
    public String outerStatus;

    public ResalePartnerProduct() {
        this.createdAt = new Date();
        this.partnerProductId = 0L;
    }
}
