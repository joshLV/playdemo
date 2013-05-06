package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.order.OuterOrder;
import models.sales.Shop;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * KTV促销活动
 */
@Entity
@Table(name = "ktv_promotions")
public class KtvPromotion extends Model {

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_promotions_shops",
            inverseJoinColumns = @JoinColumn(name = "shop_id"),
            joinColumns = @JoinColumn(name = "promotion_id"))
    public Set<Shop> shops;

    @ElementCollection(targetClass=KtvRoomType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name="ktv_promotions_room_types")
    @Column(name="room_type")
    public Set<KtvRoomType> roomTypes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "promotion")
    @OrderBy("id")
    public List<KtvPromotionConfig> promotionConfigs;

    /**
     * 促销名称(不能超过5个汉字)
     */
    @Required
    @MaxSize(5)
    public String name;

    /**
     * 促销类型
     */
    @Required
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
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



    @OneToMany(mappedBy = "promotion")
    public List<KtvPromotionConfig> promotionItem;


    @Column(name = "created_at")
    public Date createdAt;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;



}
