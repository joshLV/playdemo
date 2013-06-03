package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.*;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import utils.CrossTableConverter;
import utils.CrossTableUtil;

import javax.persistence.Query;
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

    public static void showDailySchedule(Shop shop, Date scheduledDay) {
        List<Shop> shops = SupplierRbac.currentUser().supplier.getShops();
        if (scheduledDay == null) {
            scheduledDay = DateUtils.truncate(new Date(), Calendar.DATE);
        }
        StringBuilder sql = new StringBuilder("select o from KtvRoomOrderInfo o where o.scheduledDay =:scheduledDay ")
                .append("and  (o.status =:dealStatus or (o.status=:lockStatus and o.createdAt >=:createdAt)) ");
        Date tenMinutesAgo = DateUtils.addMinutes(new Date(), -KtvRoomOrderInfo.LOCK_MINUTE);

        Map<String, Object> params = new HashMap<String, Object>();
        if (shop.id != null) {
            sql.append(" and o.shop=:shop");
            params.put("shop", shop);
        }
        sql.append("order by scheduledTime");

        params.put("scheduledDay", scheduledDay);
        params.put("dealStatus", KtvOrderStatus.DEAL);
        params.put("lockStatus", KtvOrderStatus.LOCK);
        params.put("createdAt", tenMinutesAgo);

        Query query = JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<KtvRoomOrderInfo> resultList = query.getResultList();
        List<Map<String, Object>> roomOrderInfoList = CrossTableUtil.generateCrossTable(resultList, converter);

        render(shops, shop, scheduledDay, roomOrderInfoList);
    }

    private static CrossTableConverter<KtvRoomOrderInfo, List<String>> converter =
            new CrossTableConverter<KtvRoomOrderInfo, List<String>>() {
                @Override
                public String getRowKey(KtvRoomOrderInfo target) {
                    return KtvTaobaoSku.humanTimeRange(target.scheduledTime, target.scheduledTime + target.product.duration);
                }

                @Override
                public String getColumnKey(KtvRoomOrderInfo target) {
                    return target.roomType.toString();
                }

                @Override
                public List<String> addValue(KtvRoomOrderInfo target, List<String> oldValue) {
                    if (target == null) {
                        return oldValue;
                    }
                    if (oldValue == null) {
                        oldValue = new ArrayList<>();
                    }
                    oldValue.add(target.orderItem.phone);
                    return oldValue;
                }
            };

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
