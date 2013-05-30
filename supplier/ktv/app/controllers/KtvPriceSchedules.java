package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
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

    public static void tableShow(Long shopId, KtvRoomType roomType, Long productId) {

        StringBuilder sql = new StringBuilder("select s from KtvPriceSchedule s join s.shopSchedules ss where 1=1 ");
        Map<String, Object> params = new HashMap<>();
        if (shopId != null) {
            sql.append(" and ss.shop.id = :shopId");
            params.put("shopId", shopId);
        }
        if (roomType != null) {
            sql.append(" and s.roomType = :roomType");
            params.put("roomType", roomType);
        }
        if (productId != null) {
            sql.append(" and s.product.id = :productId");
            params.put("productId", productId);
        }
        Query query = JPA.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<KtvPriceSchedule> priceScheduleList = query.getResultList();

        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shops = Shop.findShopBySupplier(supplierId);
        List<KtvProduct> products = KtvProduct.findProductBySupplier(supplierId);

        render(priceScheduleList, shopId, roomType, productId, shops, products);
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

    public static void showUpdate(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);
        List<KtvProduct> ktvProductList = KtvProduct.find("supplier=?", supplier).fetch();

        //将已选门店和日期做成json
        List<KtvShopPriceSchedule> shopPriceScheduleList = KtvShopPriceSchedule.find("bySchedule", priceSchedule).fetch();
        List<Map<String, Object>> shopPriceScheduleJsonList = new ArrayList<>();
        for (KtvShopPriceSchedule shopPriceSchedule : shopPriceScheduleList) {
            Map<String, Object> m = new HashMap<>();
            m.put("roomCount", shopPriceSchedule.roomCount);
            m.put("shopId", shopPriceSchedule.shop.id);
            shopPriceScheduleJsonList.add(m);
        }
        List<KtvDateRangePriceSchedule> dateRangePriceScheduleList = KtvDateRangePriceSchedule.find("bySchedule", priceSchedule).fetch();
        List<Map<String, Object>> dateRangePriceScheduleJsonList = new ArrayList<>();
        for (KtvDateRangePriceSchedule dateRangePriceSchedule : dateRangePriceScheduleList) {
            Map<String,Object> m = new HashMap<>();
            m.put("startDay", dateRangePriceSchedule.startDay);
            m.put("endDay", dateRangePriceSchedule.endDay);
            dateRangePriceScheduleJsonList.add(m);
        }
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        String shopPriceScheduleJson = gson.toJson(shopPriceScheduleJsonList);
        String dateRangePriceScheduleJson = gson.toJson(dateRangePriceScheduleJsonList);
        render("KtvPriceSchedules/showAdd.html",priceSchedule, shops, ktvProductList,
                shopPriceScheduleJson, dateRangePriceScheduleJson);

    }

    public static void createOrUpdate(KtvPriceSchedule priceStrategy, @As(";") Set<String> days, Long id) {
        KtvPriceSchedule oldPriceSchedule = null;
        String error = null;
        if (id != null) {
            oldPriceSchedule = KtvPriceSchedule.findById(id);
            if (oldPriceSchedule == null) {
                error ="不存在的价格策略";
                render("KtvPriceSchedules/result.html", error);
            }
        }

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
        error = validStrategy(priceStrategy.product, priceStrategy.roomType,
                shopCountMap.keySet(), scheduleDays, startTimes);

        if (error != null) {
            render("KtvPriceSchedules/result.html", error);
        }

        //时间碰撞检测
        KtvPriceSchedule collisionSchedule = collisionDetection( priceStrategy.product,  priceStrategy.roomType,
                shopCountMap.keySet(), scheduleDays, startTimes, id == null ? -1L : id);
        if (collisionSchedule != null) {
            error = "价格策略有冲突，请重新选择";
            render("KtvPriceSchedules/result.html", error);
        }

        KtvPriceSchedule currentSchedule;

        if (oldPriceSchedule == null){
            //保存策略
            priceStrategy.save();
            currentSchedule = priceStrategy;
        }else {
            oldPriceSchedule.price = priceStrategy.price;
            oldPriceSchedule.startTimes = priceStrategy.startTimes;
            oldPriceSchedule.roomType = priceStrategy.roomType;
            oldPriceSchedule.save();
            currentSchedule = oldPriceSchedule;

            //删除老的关联关系
            KtvDateRangePriceSchedule.delete("schedule = ?", oldPriceSchedule);
            KtvShopPriceSchedule.delete("schedule = ?", oldPriceSchedule);
        }

        //保存策略的日期范围列表
        for (Map.Entry<Date, Date> entry : scheduleDays.entrySet()) {
            KtvDateRangePriceSchedule schedule = new KtvDateRangePriceSchedule();
            schedule.startDay = entry.getKey();
            schedule.endDay = entry.getValue();
            schedule.schedule = currentSchedule;
            schedule.save();
        }

        //保存门店策略
        for (Map.Entry<Shop, Integer> entry : shopCountMap.entrySet()) {
            KtvShopPriceSchedule strategy = new KtvShopPriceSchedule();
            strategy.shop = entry.getKey();
            strategy.roomCount = entry.getValue();
            strategy.schedule = currentSchedule;
            strategy.save();
        }

        JPA.em().flush();

        //把价格策略加到mq
        KtvSkuMessageUtil.send(priceStrategy.id, null);

        Long shopId = shopCountMap.keySet().iterator().next().id;
        render("KtvPriceSchedules/result.html", currentSchedule, shopId);
    }

    //
    public static void make(long priceScheduleId) {
       KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(priceScheduleId);
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
              @As(",") Set<Integer> startTimes, @As(",") Set<Long> shopIds, Long excludeProductId) {
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
        KtvPriceSchedule schedule = collisionDetection( priceStrategy.product,  priceStrategy.roomType,
                shops, scheduleDays, startTimes, excludeProductId);
        if (schedule != null) {
            renderJSON("{\"scheduleId\":" + schedule.id +  "}");
        }
        renderJSON("{\"isOk\":true}");
    }

    private static String validStrategy( KtvProduct product, KtvRoomType roomType,
            Set<Shop> shops, Map<Date, Date> scheduleDays, Set<Integer> startTimesSet) {
        if (scheduleDays.size() == 0) {
            return "请选择日期范围";
        }
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
    private static KtvPriceSchedule collisionDetection( KtvProduct product,  KtvRoomType roomType,
            Set<Shop> shops, Map<Date, Date> scheduleDays, Set<Integer> startTimesSet, Long excludeProductId) {
        if (scheduleDays.size() == 0 || startTimesSet.size() ==0 || shops.size() ==0 || product == null || roomType == null) {
            throw new IllegalArgumentException("invalid param");
        }
        StringBuilder sql = new StringBuilder("select k from KtvPriceSchedule k join k.shopSchedules ks " +
                "join k.dateRanges kd where k.product = :product " +
                "and k.roomType = :roomType and ks.shop in :shops and k.id != :pid and ( ");

        //拼凑日期范围SQL
        int index =0;
        List<String> dateRanges = new ArrayList<>();
        Map<String, Date> dateParams = new HashMap<>();
        for (Map.Entry<Date, Date> entry : scheduleDays.entrySet()) {
            Date startDay = entry.getKey();
            Date endDay = entry.getValue();
            dateRanges.add(String.format("(kd.startDay <=  :endDay%s and kd.endDay >= :startDay%s)", index, index));
            dateParams.put("startDay" + index, startDay);
            dateParams.put("endDay" + index, endDay);
            index += 1;
        }
        sql.append(StringUtils.join(dateRanges, " or ")).append(")");

        Query query = JPA.em().createQuery(sql.toString());

        query.setParameter("pid", excludeProductId);
        query.setParameter("product", product);
        query.setParameter("roomType", roomType);
        query.setParameter("shops", shops);
        for (Map.Entry<String, Date> entry : dateParams.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<KtvPriceSchedule> priceSchedules = query.getResultList();

        for (KtvPriceSchedule priceSchedule : priceSchedules) {
            for (Integer startTime : priceSchedule.getStartTimesAsSet()) {
                for (Integer i : startTimesSet) {
                    if ((startTime - product.duration) < i && i < (startTime + product.duration)) {
                        return priceSchedule;
                    }
                }
            }
        }
        return null;
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
