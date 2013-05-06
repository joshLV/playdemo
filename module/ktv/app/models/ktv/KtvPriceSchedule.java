package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.beans.Transient;
import java.math.BigDecimal;
import java.util.*;

/**
 * KTV价格策略.
 */
@Entity
@Table(name = "ktv_price_schedules")
public class KtvPriceSchedule extends Model {
    @Column(name = "day_of_weeks")
    public String dayOfWeeks;

    @Column(name = "start_day")
    public Date startDay;

    @Column(name = "end_day")
    public Date endDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    public KtvRoomType roomType;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "duration")
    public int duration;//欢唱时长

    @Column(name = "start_times")// 例如"9,13"
    public String startTimes;

    @Column(name = "price")
    public BigDecimal price;//该时长所对应的价格

    @Column(name = "created_at")
    public Date createdAt;

    public KtvPriceSchedule() {
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
    }


    @Transient
    public Set<Integer> getDayOfWeeksAsSet() {
        String[] weekDayArray = dayOfWeeks.split(",");
        Set<Integer> result = new HashSet<>();
        for (String weekDay : weekDayArray) {
            result.add(Integer.parseInt(weekDay));
        }
        return result;
    }

    @Transient
    public Set<Integer> getStartTimesAsSet() {
        String[] startTimeArray = startTimes.split(",");
        Set<Integer> result = new HashSet<>();
        for (String startTime : startTimeArray) {
            result.add(Integer.parseInt(startTime));
        }
        return result;
    }





//    public static KtvPriceSchedule findPrice(Date scheduledDay, String scheduledTime, KtvRoomType roomType) {
//        Logger.info("startDay=" + scheduledDay + ", startTime=" + scheduledTime + ", roomType=" + roomType);
//        return KtvPriceSchedule.find("startDay<=? and endDay>=? and startTime<=? and endTime >=? and roomType=?", scheduledDay, scheduledDay, scheduledTime, scheduledTime, roomType).first();
//    }
//
//    /**
//     * 根据门店包厢取得价格信息
//     */
//    public static List<KtvPriceSchedule> getKtvPriceSchedules(Date startDay, Date endDay, Shop shop, models.ktv.KtvRoomType roomType) {
//        return KtvPriceSchedule.find("select k from KtvPriceSchedule k join k.shops s where (k.startDay <= ?  and k.endDay >= ?) " +
//                "and k.roomType=? and s.id =?", endDay, startDay, roomType, shop.id).fetch();
//    }

//    /**
//     * 根据门店取得相应包厢价格信息
//     */
//    public static List<KtvPriceSchedule> getSchedulesByShop(Date scheduledDay, Shop shop) {
//        return KtvPriceSchedule.find("select k from KtvPriceSchedule k join k.shops s where s.id =? and k.startDay<=? and k.endDay>=? ", shop.id, scheduledDay, scheduledDay).fetch();
//    }
//
//    public static List<KtvPriceSchedule> getSchedules(Long id, KtvPriceSchedule priceSchedule) {
//        StringBuilder sq = new StringBuilder("roomType = ? and startDay>=?");
//        List<Object> list = new ArrayList<>();
//        list.add(priceSchedule.roomType);
//        list.add(priceSchedule.startDay);
//        if (id != null) {
//            sq.append(" and id <> ?");
//            list.add(id);
//        }
//        return KtvPriceSchedule.find(sq.toString(), list.toArray()).fetch();
//    }
//
//    public static Map<String, Object> dailyScheduleOverview(Long shopId, Date day) {
//        Shop shop = Shop.findById(shopId);
//        if (shop == null) {
//            return null;
//        }
//        Map<String, Object> jsonParams = new HashMap<>();
//
//        List<KtvRoom> roomList = KtvRoom.findByShop(shop);
//        List<Map<String, Object>> rooms = new ArrayList<>();
//        for (KtvRoom ktvRoom : roomList) {
//            Map<String, Object> roomsInfo = new HashMap<>();
//            roomsInfo.put("id", ktvRoom.id);
//            roomsInfo.put("name", ktvRoom.roomType.name);
//            roomsInfo.put("type", ktvRoom.roomType.id);
//            rooms.add(roomsInfo);
//        }
//
//        List<KtvPriceSchedule> schedules = KtvPriceSchedule.getSchedulesByShop(day, shop);
//        List<Map<String, Object>> prices = new ArrayList<>();
//        for (KtvPriceSchedule schedule : schedules) {
//            Map<String, Object> scheduleInfo = new HashMap<>();
//            scheduleInfo.put("weekday", schedule.dayOfWeeks);
//            scheduleInfo.put("startTime", schedule.startTimes);
//            scheduleInfo.put("price", schedule.price);
//            scheduleInfo.put("roomType", schedule.roomType);
//            prices.add(scheduleInfo);
//        }
//        List<KtvRoomOrderInfo> scheduledRoomList = KtvRoomOrderInfo.findScheduledInfos(day, shop);
//        List<Map<String, Object>> scheduleList = new ArrayList<>();
//        for (KtvRoomOrderInfo orderInfo : scheduledRoomList) {
//            Map<String, Object> scheduleRoomInfo = new HashMap<>();
//            scheduleRoomInfo.put("roomId", orderInfo.ktvRoom.id);
//            scheduleRoomInfo.put("roomTime", orderInfo.scheduledTime);
//            scheduleList.add(scheduleRoomInfo);
//        }
//
//        jsonParams.put("rooms", rooms);
//        jsonParams.put("prices", prices);
//        jsonParams.put("schedules", scheduleList);
//
//        return jsonParams;
//    }
}
