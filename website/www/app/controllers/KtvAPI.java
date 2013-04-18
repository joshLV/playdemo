package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomOrderInfo;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.ResalerProduct;
import models.sales.Shop;
import play.mvc.Controller;

import java.util.*;

/**
 * User: yan
 * Date: 13-4-17
 * Time: 下午8:35
 */
public class KtvAPI extends Controller {

    public static void jsonRoom(String productId, Date day) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null || goods.status != GoodsStatus.ONSALE) {
            error("no goods!");
        }
        Map<String, Object> jsonParams = new HashMap<>();

        Collection<Shop> shops = goods.getShopList();
        Shop shop = shops.iterator().next();
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

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        renderJSON(gson.toJson(jsonParams));
    }

}
