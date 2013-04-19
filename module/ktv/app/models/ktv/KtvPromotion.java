package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.solr.SolrField;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * KTV促销活动
 */
@Entity
@Table(name="ktv_promotions")
public class KtvPromotion extends Model {

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_promotions_shops",
            inverseJoinColumns = @JoinColumn(name = "shop_id"),
            joinColumns = @JoinColumn(name = "ktv_promotion_id"))
    public Set<Shop> shops;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ktvPromotion")
    @OrderBy("id")
    public List<KtvPromotionConfig> promotionConfigs;

    /**
     * 促销名称(不能超过5个汉字)
     */
    @Required
//    @Max(20)
    public String name;

    /**
     * 促销类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ktv_promotion_type")
    @SolrField
    public KtvPromotionType promotionType;

    /**
     * 开始日期
     */
    @Required
    @Column(name = "start_day")
    @Temporal(TemporalType.DATE)
    public Date startDay;

    /**
     * 结束日期
     */
    @Required
    @Column(name = "end_day")
    public Date endDay;

    /**
     * 适用开始时间，如： 09:00
     */
    @Column(name = "start_time")
    public String startTime;

    /**
     * 适用结束时间，如: 12:00
     */
    @Column(name = "end_time")
    public String endTime;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_promotions_room_types",
            inverseJoinColumns = @JoinColumn(name = "room_type_id"),
            joinColumns = @JoinColumn(name = "ktv_promotion_id"))
    public Set<KtvRoomType> roomTypes;

    @OneToMany(mappedBy = "salesPromotion")
    public List<KtvPromotionItem> promotionItem;


    @Column(name = "created_at")
    public Date createdAt;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

}
