package models.sales;

import com.taobao.api.internal.util.StringUtils;
import com.uhuila.common.constants.DeletedStatus;
import models.resale.Resaler;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Column(name = "url", unique = true)
    public String url;
    @Required
    public String tag;
    /**
     * 上架时间
     */
    @Column(name = "onsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date onSaleAt;

    /**
     * 下架时间
     */
    @Column(name = "offsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date offSaleAt;

    @Enumerated(EnumType.STRING)
    public ChannelGoodsInfoStatus status;

    @Column(name = "operate_name")
    public String operateName;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "created_at")
    public Date createdAt;

    public ChannelGoodsInfo(Goods goods, Resaler resaler, String url, String tag, String operateName) {
        this.goods = goods;
        this.resaler = resaler;
        this.url = url;
        this.tag = tag;
        this.operateName = operateName;
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
    }

    public static List<ChannelGoodsInfo> findByGoods(Goods goods, Resaler resaler) {
        if (resaler == null) {
            return ChannelGoodsInfo.find("goods=? and deleted=0 order by resaler desc", goods).fetch();
        }
        return ChannelGoodsInfo.find("goods=? and resaler=? and deleted=0 order by resaler desc", goods, resaler).fetch();
    }

    public static ChannelGoodsInfo findByResaler(Resaler resaler, String url) {
        return ChannelGoodsInfo.find("resaler=? and url =? and deleted=0 order by id desc", resaler, url).first();
    }
}
