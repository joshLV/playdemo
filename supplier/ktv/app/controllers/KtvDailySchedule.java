package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvProductGoods;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvShopPriceSchedule;
import models.sales.Shop;
import org.apache.commons.lang.time.DateUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * @author likang
 *         Date: 13-4-19
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvDailySchedule extends Controller {

    public static void index(Long shopId) {
        List<Shop> shops = SupplierRbac.currentUser().supplier.getShops();
        if (shopId == null && shops.size() > 0) {
            shopId = shops.get(0).id;
        }

        render(shops, shopId);
    }

    public static void jsonRoom(Shop shop, Date day) {
        day = DateUtils.truncate(day, Calendar.DATE);
        Map<String, Object> result = new HashMap<>();

        //查出与该KTV商品有关联的当天的所有价格策略
        List<KtvShopPriceSchedule> shopPriceSchedules = KtvShopPriceSchedule.find(
                "select k from KtvShopPriceSchedule k where k.shop = ? and " +
                        "k.schedule.startDay <= ? and k.schedule.endDay >= ? and k.schedule.deleted = ?",
                shop, day, day, DeletedStatus.UN_DELETED).fetch();
        List<Map<String, Object>> priceScheduleMaps = new ArrayList<>();
        for (KtvShopPriceSchedule shopPriceSchedule : shopPriceSchedules) {
            Map<String, Object> priceScheduleMap = new HashMap<>();
            priceScheduleMap.put("startDay", shopPriceSchedule.schedule.startDay);
            priceScheduleMap.put("endDay", shopPriceSchedule.schedule.endDay);
            priceScheduleMap.put("dayOfWeeks", shopPriceSchedule.schedule.dayOfWeeks);
            priceScheduleMap.put("roomType", shopPriceSchedule.schedule.roomType);
            priceScheduleMap.put("duration", shopPriceSchedule.schedule.product.duration);
            priceScheduleMap.put("product", shopPriceSchedule.schedule.product.id);
            priceScheduleMap.put("price", shopPriceSchedule.schedule.price);
            priceScheduleMap.put("startTimes", shopPriceSchedule.schedule.startTimes);
            priceScheduleMap.put("roomCount", shopPriceSchedule.roomCount);
            priceScheduleMaps.add(priceScheduleMap);
        }

        result.put("schedules", priceScheduleMaps);

        //查出与该KTV商品有关联的当天的所有订单信息
        List<KtvRoomOrderInfo> orderInfoList = KtvRoomOrderInfo.findScheduled(day, shop);
        List<Map<String, Object>> orderInfoMaps = new ArrayList<>();
        for (KtvRoomOrderInfo orderInfo : orderInfoList) {
            Map<String, Object> o = new HashMap<>();
            o.put("roomType", orderInfo.roomType);
            o.put("scheduledDay", orderInfo.scheduledDay);
            o.put("scheduledTime", orderInfo.scheduledTime);
            o.put("duration", orderInfo.product.duration);
            o.put("phone", orderInfo.orderItem.phone);
            orderInfoMaps.add(o);
        }
        result.put("orders", orderInfoMaps);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(result));
    }

}
