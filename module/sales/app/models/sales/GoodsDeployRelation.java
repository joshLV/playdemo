package models.sales;

import models.order.OuterOrderPartner;
import play.db.jpa.Model;
import models.sales.Goods;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 上午11:43
 */
@Entity
@Table(name = "goods_deploy_relation")
public class GoodsDeployRelation extends Model {
    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Column(name = "created_at")
    public Date createAt;

    @Column(name = "link_id")
    public Long linkId;

    public static GoodsDeployRelation generate(Goods goods, OuterOrderPartner partner) {
        GoodsDeployRelation deployRelation = new GoodsDeployRelation();
        deployRelation.partner = partner;
        deployRelation.goods = goods;
        deployRelation.createAt = new Date();
        deployRelation.save();
        deployRelation.linkId = deployRelation.id + 10000;
        deployRelation.save();
        return deployRelation;
    }

    public static Goods getGoods(OuterOrderPartner partner, Long linkId) {
        GoodsDeployRelation deployRelation = GoodsDeployRelation.find("partner=? and linkId=?", partner, linkId).first();
        return deployRelation != null ? deployRelation.goods : null;
    }

    public static GoodsDeployRelation getDeployRelationGoods(OuterOrderPartner partner, Goods goods) {
        GoodsDeployRelation deployRelation = GoodsDeployRelation.find("partner=? and goods=?", partner, goods).first();
        return deployRelation;
    }
}
