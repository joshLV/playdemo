package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import models.supplier.Supplier;
import models.taobao.KtvSkuMessageUtil;
import org.apache.commons.lang.StringUtils;
import models.ktv.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import play.data.binding.As;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-4-12
 * Time: 下午2:09
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvPriceSchedules extends Controller {
    /**
     * 价格策略页面
     */
    public static void index(Long shopId, KtvRoomType roomType, Long productId) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shops = Shop.findShopBySupplier(supplierId);

        Shop shop = null;
        KtvProduct product = null;
        if (shopId != null) {
            shop = Shop.find("bySupplierIdAndId", supplierId, shopId).first();
        }
        if (shop == null && shops.size() > 0) {
            shop = shops.get(0);
        }
        if (roomType == null) {
            roomType = KtvRoomType.values()[0];
        }
        if (productId != null) {
            product = KtvProduct.find("id = ? and supplier.id = ?", productId, supplierId).first();
        }

        if (shop != null) {
            List<KtvProduct> products = KtvProduct.findProductBySupplier(shop.supplierId);
            renderArgs.put("products", products);

            if (product == null && products.size() > 0) {
                product = products.get(0);
            }
        }
        render(shops, shop, roomType, product);
    }

    public static void jsonSearch(Date startDay, Date endDay, Shop shop, KtvRoomType roomType, KtvProduct product) {
        if (startDay.after(endDay)) {
            error();
        }
        List<KtvPriceSchedule> priceSchedules = KtvShopPriceSchedule.find(
                "select k.schedule from KtvShopPriceSchedule k where k.shop = ? and k.schedule.roomType = ? " +
                        "and k.schedule.startDay <= ? and k.schedule.endDay >= ? and k.schedule.product = ?",
                shop, roomType, endDay, startDay, product).fetch();
        List<Map<String, Object>> result = new ArrayList<>();
        for (KtvPriceSchedule schedule : priceSchedules) {
            Map<String, Object> o = new HashMap<>();
            o.put("startDay", schedule.startDay);
            o.put("endDay", schedule.endDay);
            o.put("duration", schedule.product.duration);
            o.put("startTimes", schedule.startTimes);
            o.put("roomType", schedule.roomType);
            o.put("dayOfWeeks", schedule.dayOfWeeks);
            o.put("price", schedule.price);
            o.put("id", schedule.getId());
            result.add(o);
        }


        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(result));

    }

    public static void showAdd() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);
        List<KtvProduct> ktvProductList = KtvProduct.find("supplier=?", supplier).fetch();
        render(shops, ktvProductList);
    }

    public static void showEdit(Long id) {
        KtvPriceSchedule priceStrategy = KtvPriceSchedule.findById(id);
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);
        List<KtvProduct> ktvProductList = KtvProduct.find("supplier=?", supplier).fetch();

        List<KtvShopPriceSchedule> shopPriceScheduleList = KtvShopPriceSchedule.find("schedule=?", priceStrategy).fetch();
        render(priceStrategy, shops, ktvProductList, shopPriceScheduleList);

    }

    public static void create(KtvPriceSchedule priceStrategy, @As(";") Set<String> days) {
        Map<Shop, Integer> shopCountMap = new HashMap<>();

        //组装门店和门店的包厢数量
        Map<String, String> allParams = params.allSimple();
        for (Map.Entry<String, String> param : allParams.entrySet()) {
            String key = param.getKey();
            if (key.startsWith("shop-")) {
                String shopIdStr = key.substring(5);
                String shopCount = param.getValue();
                if (NumberUtils.isDigits(shopIdStr) && NumberUtils.isDigits(shopCount)) {
                    Long shopId = Long.parseLong(shopIdStr);
                    Shop shop = Shop.findById(shopId);
                    if (shop != null) {
                        shopCountMap.put(shop, Integer.parseInt(shopCount));
                    }
                }
            }
        }
        Set<Integer> startTimes = priceStrategy.getStartTimesAsSet();

        Map<Date, Date> scheduleDays = scheduleDaysFromInput(days);

        //检测参数合法性
        String error = validStrategy(priceStrategy.product, priceStrategy.roomType,
                shopCountMap.keySet(), scheduleDays, startTimes);

        if (error != null) {
            error(error);
        }

        //时间碰撞检测
        Map<String, Object> collisions = collisionDetection(
                priceStrategy.product,  priceStrategy.roomType, shopCountMap.keySet(), scheduleDays, startTimes);
        if (collisions != null) {
            error("价格策略有冲突，请重新选择");
        }

        //保存策略
        priceStrategy.save();

        //保存策略的日期范围列表
        for (Map.Entry<Date, Date> entry : scheduleDays.entrySet()) {
            KtvDateRangePriceSchedule schedule = new KtvDateRangePriceSchedule();
            schedule.startDay = entry.getKey();
            schedule.endDay = entry.getValue();
            schedule.schedule = priceStrategy;
            schedule.save();
        }

        //保存门店策略
        for (Map.Entry<Shop, Integer> entry : shopCountMap.entrySet()) {
            KtvShopPriceSchedule strategy = new KtvShopPriceSchedule();
            strategy.shop = entry.getKey();
            strategy.roomCount = entry.getValue();
            strategy.schedule = priceStrategy;
            strategy.save();

        }

        JPA.em().flush();

        //把价格策略加到mq
        KtvSkuMessageUtil.send(priceStrategy.id, null);

        index(shopCountMap.keySet().iterator().next().id, priceStrategy.roomType, priceStrategy.product.id);
    }

    //
    public static void make(long priceScheduleId) {
       KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(priceScheduleId);
//        List<KtvProductGoods> ktvProductGoodsList = KtvProductGoods.findAll();
//        for (KtvProductGoods productGoods : ktvProductGoodsList) {
//            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
//        }
//        ECoupon eCoupon = ECoupon.findById(63959L);
//        //ktv商户
//        //更新淘宝ktv sku信息
//        KtvProductGoods ktvProductGoods = KtvProductGoods.find("goods=?", eCoupon.goods).first();
//        if (ktvProductGoods != null) {
//            KtvRoomOrderInfo ktvRoomOrderInfo = KtvRoomOrderInfo.find("orderItem=?", eCoupon.orderItems).first();
//            ktvRoomOrderInfo.status = KtvOrderStatus.REFUND;
//            ktvRoomOrderInfo.save();
//
//            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(ktvProductGoods);
//
//            Logger.info("after ecoupon refund,update taobao ktv sku:ktvProductGoods.id:" + ktvProductGoods.id + " success");
//        }
    }

    private static Map<Date, Date> scheduleDaysFromInput(Collection<String> days) {
        Map<Date, Date> daysMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        for (String dayPair : days) {
            String [] dayPairArray = dayPair.split("-");
            if (dayPairArray.length != 2) {
                continue;
            }
            try{
                Date start = dateFormat.parse(dayPairArray[0]);
                Date end = dateFormat.parse(dayPairArray[1]);
                daysMap.put(start, end);
            }catch (ParseException e) {
                continue;
            }
        }
        return daysMap;
    }


    public static void jsonCollisionDetection(KtvPriceSchedule priceStrategy, @As(";") Set<String> days,
                                              @As(",") Set<Integer> startTimes, @As(",") Set<Long> shopIds) {
        //构建商店列表
        Set<Shop> shops = new HashSet<>();
        for (Long shopId : shopIds) {
            Shop shop = Shop.findById(shopId);
            if (shop != null) {
                shops.add(shop);
            }
        }
        Map<Date, Date> scheduleDays = scheduleDaysFromInput(days);

        //检测参数合法性
        String error = validStrategy(priceStrategy.product, priceStrategy.roomType, shops, scheduleDays, startTimes);

        if (error != null) {
            renderJSON("{\"error\":\"" + error + "\"}");
        }

        //时间碰撞检测
        Map<String, Object> collisions = collisionDetection( priceStrategy.product,  priceStrategy.roomType,
                shops, scheduleDays, startTimes);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        if (collisions != null) {
            Date startDay = (Date) collisions.get("startDay");
            Date endDay = (Date) collisions.get("endDay");
            Integer startTime = (Integer) collisions.get("startTime");
            renderJSON("{\"startDay\":\"" + dateFormat.format(startDay) + "\"," +
                    "\"endDay\":\"" + dateFormat.format(endDay) + "\"," +
                    "\"startTime\":\"" + startTime + "\"}");
        }
        renderJSON("{\"isOk\":true}");
    }

    private static String validStrategy( KtvProduct product, KtvRoomType roomType,
            Set<Shop> shops, Map<Date, Date> scheduleDays, Set<Integer> startTimesSet) {
        for (Map.Entry<Date, Date> entry : scheduleDays.entrySet()) {
            if (entry.getKey().compareTo(entry.getValue()) > 0) {
                return "起始时间不能大于结束时间";
            }
        }
        List<Date> scheduleStartDays = new ArrayList<>(scheduleDays.keySet());
        for (int i = 0; i < scheduleDays.size(); i ++) {
            for (int j = i + 1 ; j < scheduleDays.size() ;j ++) {
                Date startDayA = scheduleStartDays.get(i);
                Date endDayA = scheduleDays.get(startDayA);

                Date startDayB = scheduleStartDays.get(j);
                Date endDayB = scheduleDays.get(startDayB);

                if (startDayB.compareTo(endDayA) <=0 && endDayB.compareTo(startDayA) >= 0) {
                    return "日期有交叉";
                }
            }
        }

        if (product == null) {
            return "无效的KTV产品";
        }

        if (roomType == null) {
            return "无效的房型";
        }
        if (shops.size() == 0) {
            return "无效的门店列表";
        }
        if (startTimesSet.size() == 0) {
            return "未设置使用时间段";
        }
        //todo 检测startTime 冲突

        return null;
    }

    //检测新建的价格策略有无时间上的碰撞
    private static Map<String, Object> collisionDetection( KtvProduct product,  KtvRoomType roomType,
            Set<Shop> shops, Map<Date, Date> scheduleDays, Set<Integer> startTimesSet) {

        for (Map.Entry<Date, Date> entry : scheduleDays.entrySet()) {
            Date startDay = entry.getKey();
            Date endDay = entry.getValue();

            Query query = JPA.em().createQuery("select k from KtvPriceSchedule k join k.shopPriceSchedules ks " +
                    "join k.dateRangePriceSchedules kd where k.product = :product " +
                    "and k.roomType = :roomType and ks.shop in :shops and kd.startDay <= :endDay and kd.endDay >= :startDay");

            query.setParameter("product", product);
            query.setParameter("endDay", endDay);
            query.setParameter("startDay", startDay);
            query.setParameter("roomType", roomType);
            query.setParameter("shops", shops);
            List<KtvPriceSchedule> priceSchedules = query.getResultList();

            for (KtvPriceSchedule priceSchedule : priceSchedules) {
                for (Integer startTime : priceSchedule.getStartTimesAsSet()) {
                    for (Integer i : startTimesSet) {
                        if ((startTime - product.duration) < i && i < (startTime + product.duration)) {
                            //冲突了
                            Map<String, Object> result = new HashMap<>();
                            result.put("startDay", startDay);
                            result.put("endDay", endDay);
                            result.put("startTime", startTime);
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void update(Long id, @Valid KtvPriceSchedule priceStrategy, List<String> useWeekDays, Shop shop) {
        BigDecimal price = priceStrategy.price;
        KtvPriceSchedule updSchedule = KtvPriceSchedule.findById(id);
        updSchedule.price = price;
        updSchedule.dayOfWeeks = StringUtils.join(useWeekDays, ",");
        updSchedule.save();

        if (updSchedule.price.compareTo(price) != 0) {
            //把价格策略加到mq
            KtvSkuMessageUtil.send(id, null);
        }
        index(shop.id, updSchedule.roomType, updSchedule.product.id);
    }

    public static void delete(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        if (priceSchedule == null) {
            index(null, null, null);
            return;
        }
        priceSchedule.delete();

        index(null, null, null);
    }
}
