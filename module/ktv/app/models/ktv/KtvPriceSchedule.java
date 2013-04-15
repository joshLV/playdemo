package models.ktv;

import com.google.gson.annotations.Expose;
import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * KTV价格策略.
 */
@Entity
@Table(name = "ktv_price_schedules")
public class KtvPriceSchedule extends GenericModel {
    @Id
    @GeneratedValue
    @Expose
    public Long id;

    public Long getId() {
        return id;
    }

    @Override
    public Object _key() {
        return getId();
    }

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_price_schedules_shops",
            inverseJoinColumns = @JoinColumn(name = "shop_id"),
            joinColumns = @JoinColumn(name = "ktv_price_schedules_id"))
    public Set<Shop> shops;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = true)
    public KtvRoomType roomType;

    /**
     * 开始日期
     */
    @Column(name = "start_day")
    @Expose
    public Date startDay;

    /**
     * 结束日期
     */
    @Column(name = "end_day")
    @Expose
    public Date endDay;

    /**
     * 指定每周的可用日.
     * 保存为数字，以逗号分隔。
     * 默认为1,2,3,4,5,6,7
     */
    @Column(name = "use_week_day")
    @Expose
    public String useWeekDay;


    /**
     * 开始时间，如： 09:00
     */
    @Column(name = "start_time")
    @Expose
    public String startTime;

    /**
     * 结束时间，如: 12:00
     */
    @Column(name = "end_time")
    @Expose
    public String endTime;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    /**
     * 每间每小时的价格
     */
    @Expose
    public BigDecimal price;

    @Column(name = "created_at")
    public Date createdAt;


    public static void update(Long id, KtvPriceSchedule schedule) {
        KtvPriceSchedule updPriceSchedule = KtvPriceSchedule.findById(id);
        updPriceSchedule.useWeekDay = schedule.useWeekDay;
        updPriceSchedule.startDay = schedule.startDay;
        updPriceSchedule.endDay = schedule.endDay;
        updPriceSchedule.startTime = schedule.startTime;
        updPriceSchedule.endTime = schedule.endTime;
        updPriceSchedule.price = schedule.price;
        updPriceSchedule.save();

    }
}
