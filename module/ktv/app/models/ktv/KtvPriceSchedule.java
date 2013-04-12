package models.ktv;

import com.google.gson.annotations.Expose;
import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * KTV价格策略.
 */
@Entity
@Table(name = "ktv_price_schedules")
public class KtvPriceSchedule extends Model {

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_price_schedule_shops",
            inverseJoinColumns = @JoinColumn(name = "shop_id"),
            joinColumns = @JoinColumn(name = "ktv_price_schedule_id"))
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
    @Column(name="end_time")
    @Expose
    public String endTime;

    /**
     * 每间每小时的价格
     */
    @Expose
    public BigDecimal price;
}
