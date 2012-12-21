package models.sales;

import models.resale.Resaler;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-20
 * Time: 下午3:52
 */
@Table(name = "channel_goods_info")
@Entity
public class ChannelGoodsInfo extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resaler_id", nullable = true)
    public Resaler resaler;

    @Required
    public String url;
    /**
     * 上架时间
     */
    @Required
    @Column(name = "onsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date onSaleAt;

    /**
     * 下架时间
     */
    @Required
    @Column(name = "offsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date offSaleAt;

    @Column(name = "operate_name")
    public String operateName;

    @Column(name = "created_at")
    public Date createdAt;


    public static List<ChannelGoodsInfo> findByGoods(Goods goods) {
        return ChannelGoodsInfo.find("goods=?", goods).fetch();
    }
}
