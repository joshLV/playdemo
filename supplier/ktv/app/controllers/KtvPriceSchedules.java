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

    public static void jsonSearch(Date startDay, Date endDay, Shop shop, KtvRoomType roomType) {
        if (startDay.after(endDay)) {
            error();
        }
        List<KtvPriceSchedule> priceSchedules = KtvShopPriceSchedule.find(
                "select k.schedule from KtvShopPriceSchedule k where k.shop = ? and k.schedule.roomType = ? " +
                        "and k.schedule.startDay <= ? and k.schedule.endDay >= ?",
                shop, roomType, endDay, startDay).fetch();
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

    public static void create(KtvPriceSchedule priceStrategy, Set<Integer> useWeekDays) {
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
        //检测参数合法性
        String error = validStrategy(priceStrategy.product.duration, priceStrategy.startDay, priceStrategy.endDay,
                priceStrategy.roomType, shopCountMap.keySet(), useWeekDays, startTimes);


        if (error != null) {
            error(error);
        }

        //时间碰撞检测
        Map<String, Object> collisions = collisionDetection(
                priceStrategy.product.duration, priceStrategy.startDay, priceStrategy.endDay,
                priceStrategy.roomType, shopCountMap.keySet(), useWeekDays, startTimes);
        if (collisions != null) {
            error("价格策略有冲突，请重新选择");
        }

        //保存策略
        priceStrategy.dayOfWeeks = StringUtils.join(useWeekDays, ",");
        priceStrategy.save();

        //保存门店策略
        for (Map.Entry<Shop, Integer> entry : shopCountMap.entrySet()) {
            KtvShopPriceSchedule strategy = new KtvShopPriceSchedule();
            strategy.shop = entry.getKey();
            strategy.roomCount = entry.getValue();
            strategy.schedule = priceStrategy;
            strategy.save();

        }
        KtvSkuMessageUtil.send(priceStrategy.id);

        index(shopCountMap.keySet().iterator().next().id, priceStrategy.roomType, priceStrategy.product.id);
    }

    //
    public static void make(long priceScheduleId) {
//       KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(priceScheduleId);
        List<KtvProductGoods> ktvProductGoodsList = KtvProductGoods.findAll();
        for (KtvProductGoods productGoods : ktvProductGoodsList) {
            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
        }
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


    public static void jsonCollisionDetection(KtvPriceSchedule priceStrategy, @As(",") Set<Integer> useWeekDays,
                                              @As(",") Set<Integer> startTimes, @As(",") Set<Long> shopIds) {
        //构建商店列表
        Set<Shop> shops = new HashSet<>();
        for (Long shopId : shopIds) {
            Shop shop = Shop.findById(shopId);
            if (shop != null) {
                shops.add(shop);
            }
        }

        //检测参数合法性
        String error = validStrategy(priceStrategy.product.duration, priceStrategy.startDay, priceStrategy.endDay,
                priceStrategy.roomType, shops, useWeekDays, startTimes);

        if (error != null) {
            renderJSON("{\"error\":\"" + error + "\"}");
        }

        //时间碰撞检测
        Map<String, Object> collisions = collisionDetection(
                priceStrategy.product.duration, priceStrategy.startDay, priceStrategy.endDay,
                priceStrategy.roomType, shops, useWeekDays, startTimes);
        if (collisions != null) {
            Shop shop = (Shop) collisions.get("shop");
            List<Integer> weekDays = (ArrayList<Integer>) collisions.get("weekdays");
            Integer startTime = (Integer) collisions.get("startTime");
            renderJSON("{\"shopName\":\"" + shop.name + "\"," +
                    "\"weekDays\":\"" + StringUtils.join(weekDays, ",") + "\"," +
                    "\"startTime\":\"" + startTime + "\"}");
        }
        renderJSON("{\"isOk\":true}");
    }

    private static String validStrategy(
            int duration, Date startDay, Date endDay, KtvRoomType roomType,
            Set<Shop> shops, Set<Integer> useWeekDaysSet, Set<Integer> startTimesSet) {
        if (duration <= 0 || duration > 6) {
            return "无效的欢唱时长";
        }
        if (startDay == null) {
            return "无效的起始日期";
        }
        if (endDay == null) {
            return "无效的结束日期";
        }

        startDay = DateUtils.truncate(startDay, Calendar.DATE);
        endDay = DateUtils.truncate(endDay, Calendar.DATE);
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        if (startDay.before(today)) {
            return "起始日期不能早于今日";
        }
        if (startDay.after(endDay)) {
            return "起始日期不能晚于结束日期";
        }

        if (roomType == null) {
            return "无效的房型";
        }
        if (shops.size() == 0) {
            return "无效的门店列表";
        }
        if (useWeekDaysSet.size() == 0) {
            return "未设置可使用日期";
        }
        if (startTimesSet.size() == 0) {
            return "未设置使用时间段";
        }


        return null;
    }

    //检测新建的价格策略有无时间上的碰撞
    private static Map<String, Object> collisionDetection(
            int duration, Date startDay, Date endDay, KtvRoomType roomType,
            Set<Shop> shops, Set<Integer> useWeekDaysSet, Set<Integer> startTimesSet) {

        //查出所有 时间交叉、欢唱时长相同、包厢类型相同、门店交叉的门店策略
        Query query = JPA.em().createQuery("select k from KtvShopPriceSchedule k where k.schedule.product.duration = :duration " +
                "and k.schedule.startDay <= :endDay and k.schedule.endDay >= :startDay and k.schedule.roomType = :roomType " +
                "and k.shop in :shops");
        query.setParameter("duration", duration);
        query.setParameter("endDay", endDay);
        query.setParameter("startDay", startDay);
        query.setParameter("roomType", roomType);
        query.setParameter("shops", shops);
        List<KtvShopPriceSchedule> shopStrategies = query.getResultList();
        //维护一个已知的 没有冲突的策略列表
        Set<Long> okStrategies = new HashSet<>();
        for (KtvShopPriceSchedule shopStrategy : shopStrategies) {
            if (okStrategies.contains(shopStrategy.schedule.id)) {
                continue;
            }
            //判断使用的星期有没有交叉
            List<Integer> weekDayIntersections = (ArrayList<Integer>) CollectionUtils.intersection(
                    shopStrategy.schedule.getDayOfWeeksAsSet(), useWeekDaysSet);
            //如果所选的星期都没有交叉，那将其假如到安全列表后直接跳过
            if (weekDayIntersections.size() == 0) {
                okStrategies.add(shopStrategy.schedule.id);
                continue;
            }
            for (Integer startTime : shopStrategy.schedule.getStartTimesAsSet()) {
                for (Integer i : startTimesSet) {
                    if ((startTime - duration) < i && i < (startTime + duration)) {
                        //冲突了
                        Map<String, Object> result = new HashMap<>();
                        result.put("shop", shopStrategy.shop);
                        result.put("weekdays", weekDayIntersections);
                        result.put("startTime", startTime);
                        return result;
                    }
                }
            }
            okStrategies.add(shopStrategy.schedule.id);
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
            KtvSkuMessageUtil.send(id);
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
