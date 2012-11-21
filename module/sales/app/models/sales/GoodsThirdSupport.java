package models.sales;

import models.order.OuterOrderPartner;
import play.db.jpa.Model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p/>
 * 向第三方推送商品，保留商品更改的记录
 * User: yanjy
 * Date: 12-11-21
 * Time: 下午4:38
 */
@Entity
@Table(name = "goods_third_support")
public class GoodsThirdSupport extends Model {
    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "goods_data")
    public String goodsData;          //此商品的完整信息

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    @Column(name = "created_at")
    public Date createdAt;

    public GoodsThirdSupport generate(Goods goods, String data, OuterOrderPartner partner) {
        this.partner = partner;
        this.goodsData = data;
        this.goods = goods;
        this.createdAt = new Date();
    }

    public static GoodsThirdSupport getSupportGoods(Goods goods, OuterOrderPartner partner) {
        GoodsThirdSupport support = GoodsThirdSupport.find("partner=? and goods=?", partner, goods).first();
        return support;
    }
}
