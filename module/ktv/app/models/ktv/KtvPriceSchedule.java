package models.ktv;

import com.google.gson.annotations.Expose;
import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import play.Logger;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

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

    @Required
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = true)
    public KtvRoomType roomType;

    /**
     * 开始日期
     */
    @Required
    @Column(name = "start_day")
    @Temporal(TemporalType.DATE)
    @Expose
    public Date startDay;

    /**
     * 结束日期
     */
    @Required
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

    @Min(1)
    @Column(name = "room_number")
    public Long roomNumber;

    /**
     * 每间每小时的价格
     */
    @Expose
    @Required
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
        updPriceSchedule.roomNumber = schedule.roomNumber;
        updPriceSchedule.save();

    }

    public static KtvPriceSchedule findPrice(Date scheduledDay, String scheduledTime, KtvRoomType roomType) {
        Logger.info("startDay=" + scheduledDay + ", startTime=" + scheduledTime + ", roomType=" + roomType);
        return KtvPriceSchedule.find("startDay<=? and endDay>=? and startTime<=? and endTime >=? and roomType=?", scheduledDay, scheduledDay, scheduledTime, scheduledTime, roomType).first();
    }

    /**
     * 根据门店包厢取得价格信息
     */
    public static List<KtvPriceSchedule> getKtvPriceSchedules(Date startDay, Date endDay, Shop shop, KtvRoomType roomType) {
        return KtvPriceSchedule.find("select k from KtvPriceSchedule k join k.shops s where (k.startDay <= ?  and k.endDay >= ?) " +
                "and k.roomType=? and s.id =?", endDay, startDay, roomType, shop.id).fetch();
    }

    /**
     * 根据门店取得相应包厢价格信息
     */
    public static List<KtvPriceSchedule> getSchedulesByShop(Date scheduledDay, Shop shop) {
        return KtvPriceSchedule.find("select k from KtvPriceSchedule k join k.shops s where s.id =? and k.startDay<=? and k.endDay>=? ", shop.id, scheduledDay, scheduledDay).fetch();
    }

    public static List<KtvPriceSchedule> getSchedules(Long id, KtvPriceSchedule priceSchedule) {
        StringBuilder sq = new StringBuilder("roomType = ? and startDay>=?");
        List list = new ArrayList();
        list.add(priceSchedule.roomType);
        list.add(priceSchedule.startDay);
        if (id != null) {
            sq.append("and id <> ?");
            list.add(id);
        }
        return KtvPriceSchedule.find(sq.toString(), list.toArray()).fetch();
    }

    public static Map<String, Object> dailyScheduleOverview(Long shopId, Date day) {
        Shop shop = Shop.findById(shopId);
        if (shop == null) {
            return null;
        }
        Map<String, Object> jsonParams = new HashMap<>();

        List<KtvRoom> roomList = KtvRoom.findByShop(shop);
        List<Map<String, Object>> rooms = new ArrayList<>();
        for (KtvRoom ktvRoom : roomList) {
            Map<String, Object> roomsInfo = new HashMap<>();
            roomsInfo.put("id", ktvRoom.id);
            roomsInfo.put("name", ktvRoom.roomType.name);
            roomsInfo.put("type", ktvRoom.roomType.id);
            rooms.add(roomsInfo);
        }

        List<KtvPriceSchedule> schedules = KtvPriceSchedule.getSchedulesByShop(day, shop);
        List<Map<String, Object>> prices = new ArrayList<>();
        for (KtvPriceSchedule schedule : schedules) {
            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("weekday", schedule.useWeekDay);
            scheduleInfo.put("startTime", schedule.startTime);
            scheduleInfo.put("endTime", schedule.endTime);
            scheduleInfo.put("price", schedule.price);
            scheduleInfo.put("roomType", schedule.roomType.id);
            prices.add(scheduleInfo);
        }
        List<KtvRoomOrderInfo> scheduledRoomList = KtvRoomOrderInfo.findScheduledInfos(day, shop);
        List<Map<String, Object>> scheduleList = new ArrayList<>();
        for (KtvRoomOrderInfo orderInfo : scheduledRoomList) {
            Map<String, Object> scheduleRoomInfo = new HashMap<>();
            scheduleRoomInfo.put("roomId", orderInfo.ktvRoom.id);
            scheduleRoomInfo.put("roomTime", orderInfo.scheduledTime);
            scheduleList.add(scheduleRoomInfo);
        }

        jsonParams.put("rooms", rooms);
        jsonParams.put("prices", prices);
        jsonParams.put("schedules", scheduleList);

        return jsonParams;
    }
}
