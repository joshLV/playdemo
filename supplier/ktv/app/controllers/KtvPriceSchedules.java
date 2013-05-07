package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import controllers.supplier.SupplierInjector;
import models.accounts.AccountType;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import models.taobao.KtvSkuMessageUtil;
import org.apache.commons.lang.StringUtils;
import models.ktv.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-4-12
 * Time: 下午2:09
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvPriceSchedules extends Controller {
    // 淘宝电子凭证的secret
    public static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    public static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");

    public static void index(Long shopId, KtvRoomType roomType) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shops = Shop.findShopBySupplier(supplierId);

        Shop shop = null;
        if (shopId != null) {
            shop = Shop.find("bySupplierIdAndId", supplierId, shopId).first();
        }
        if (shop == null && shops.size() > 0) {
            shop = shops.get(0);
        }
        if (roomType == null) {
            roomType = KtvRoomType.values()[0];
        }
        render(shops, shop, roomType);
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

        index(shopCountMap.keySet().iterator().next().id, priceStrategy.roomType);
    }

    public static void make(Long priceScheduleId) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(priceScheduleId);
        List<KtvShopPriceSchedule> shopPriceSchedules = KtvShopPriceSchedule.find("bySchedule", priceSchedule).fetch();
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
        //找到淘宝的token
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        //此价格策略所涉及的所有门店
        for (KtvShopPriceSchedule shopPriceSchedule : shopPriceSchedules) {
            //每个门店所关联的所有ktv产品
            List<KtvProductGoods> productGoodsList = KtvProductGoods.find("byShopAndProduct",
                    shopPriceSchedule.shop, shopPriceSchedule.schedule.product).fetch();
            for (KtvProductGoods productGoods : productGoodsList) {
                //这个产品所对应的所有价格策略

                List<KtvTaobaoSaleProperty> newSaleProperties = buildKtvTaobaoSaleProperties(shopPriceSchedule, productGoods);

                List<KtvTaobaoSaleProperty> oldSaleProperties = KtvTaobaoSaleProperty.find("byGoods", productGoods.goods).fetch();
                Map<String, List<KtvTaobaoSaleProperty>> diffResult = diffTaobaoSaleProperties(newSaleProperties, oldSaleProperties);

                //更新每一个该ktv产品所对应的分销渠道上的商品
                List<ResalerProduct> resalerProductList = ResalerProduct.find("byGoodsAndPartner", productGoods.goods, OuterOrderPartner.TB).fetch();

                for (ResalerProduct resalerProduct : resalerProductList) {
                    if (StringUtils.isBlank(resalerProduct.partnerProductId)) {
                        continue;
                    }

                    for (Map.Entry<String, List<KtvTaobaoSaleProperty>> entry : diffResult.entrySet()) {

                        switch (entry.getKey()) {
                            case "add":
                                for (KtvTaobaoSaleProperty p : entry.getValue()) {
                                    addSalePropertyOnTaobao(taobaoClient, token, resalerProduct, p);
                                }
                                break;
                            case "update":
                                for (KtvTaobaoSaleProperty p : entry.getValue()) {
                                    updateSalePropertyOnTaobao(taobaoClient, token, resalerProduct, p);
                                }
                                break;
                            case "delete":
                                for (KtvTaobaoSaleProperty p : entry.getValue()) {
                                    deleteSalePropertyOnTaobao(taobaoClient, token, resalerProduct, p);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    private static void deleteSalePropertyOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSaleProperty p) {
        ItemSkuUpdateRequest req = new ItemSkuUpdateRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));

        req.setProperties(p.roomType + ";$日期:" + p.date + ";$欢唱时间:" + p.timeRange);
        req.setQuantity((long) p.quantity);
        req.setPrice(p.price.toString());

        try {
            ItemSkuUpdateResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode())) {
                p.delete();
            }
        } catch (ApiException e) {
            Logger.info(e, "add sku to taobao failed");
        }
    }

    private static void updateSalePropertyOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSaleProperty p) {
        ItemSkuUpdateRequest req = new ItemSkuUpdateRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));

        req.setProperties(p.roomType + ";$日期:" + p.date + ";$欢唱时间:" + p.timeRange);
        req.setQuantity((long) p.quantity);
        req.setPrice(p.price.toString());

        try {
            ItemSkuUpdateResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode())) {
                p.save();
            }
        } catch (ApiException e) {
            Logger.info(e, "add sku to taobao failed");
        }
    }

    private static void addSalePropertyOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSaleProperty p) {
        ItemSkuAddRequest req = new ItemSkuAddRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));

        req.setProperties(p.roomType + ";$日期:" + p.date + ";$欢唱时间:" + p.timeRange);
        req.setQuantity((long) p.quantity);
        req.setPrice(p.price.toString());

        try {
            ItemSkuAddResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode())) {
                p.save();
            }
        } catch (ApiException e) {
            Logger.info(e, "add sku to taobao failed");
        }
    }


    private static Map<String, List<KtvTaobaoSaleProperty>> diffTaobaoSaleProperties(
            List<KtvTaobaoSaleProperty> newSaleProperties, List<KtvTaobaoSaleProperty> oldSaleProperties) {
        for (KtvTaobaoSaleProperty property : newSaleProperties) {
            property.makeIdentity();
        }
        for (KtvTaobaoSaleProperty property : oldSaleProperties) {
            property.makeIdentity();
        }

        Map<String, List<KtvTaobaoSaleProperty>> result = new HashMap<>();
        List<KtvTaobaoSaleProperty> tobeUpdated = new ArrayList<>();
        List<KtvTaobaoSaleProperty> tobeAdded = new ArrayList<>();
        List<KtvTaobaoSaleProperty> tobeDeleted = new ArrayList<>(oldSaleProperties);


        for (KtvTaobaoSaleProperty newProperty : newSaleProperties) {
            boolean found = false;
            for (KtvTaobaoSaleProperty oldProperty : tobeDeleted) {
                //如果有相同的，把新值赋给旧值（旧值在数据库中，方便之后的save），然后将旧值从tobeDeleted转移到tobeUpdated
                if (newProperty.identity.equals(oldProperty.identity)) {
                    oldProperty.quantity = newProperty.quantity;
                    oldProperty.price = newProperty.price;

                    tobeUpdated.add(oldProperty);
                    tobeDeleted.remove(oldProperty);
                    found = true;
                    break;
                }
            }
            //没有相同的，就是新值，添加到tobeAdded
            if (!found) {
                tobeAdded.add(newProperty);
            }
        }

        result.put("update", tobeUpdated);
        result.put("add", tobeAdded);
        result.put("delete", tobeDeleted);
        return result;
    }

    private static List<KtvTaobaoSaleProperty> buildKtvTaobaoSaleProperties(KtvShopPriceSchedule shopPriceSchedule, KtvProductGoods productGoods) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);

        Query query = JPA.em().createQuery("select s.schedule from KtvShopPriceSchedule s where s.shop = :shop "
                + "and s.schedule.product = :product and s.schedule.startDay <= :endDay and s.schedule.endDay >= :startDay and s.schedule.deleted = :deleted");

        Date startDay = DateUtils.truncate(new Date(), Calendar.DATE);
        Date endDay = DateUtils.addDays(startDay, 6);

        query.setParameter("shop", productGoods.shop);
        query.setParameter("product", productGoods.product);
        query.setParameter("startDay", startDay);
        query.setParameter("endDay", endDay);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        List<KtvPriceSchedule> priceScheduleList = query.getResultList();

        List<KtvTaobaoSaleProperty> saleProperties = new ArrayList<>();

        List<KtvRoomOrderInfo> roomOrderInfoList = KtvRoomOrderInfo.find("goods=? and shop=? and scheduledDay = ?",
                productGoods.goods, shopPriceSchedule.shop, today).fetch();

        for (int i = 0; i < 7; i++) {
            Date day = DateUtils.addDays(startDay, i);
            for (KtvPriceSchedule ps : priceScheduleList) {
                calendar.setTime(day);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                dayOfWeek = (dayOfWeek == 1) ? dayOfWeek + 6 : dayOfWeek - 1;
                //如果日期范围不包括当天，跳过
                if (ps.startDay.after(day) || ps.endDay.before(day)) {
                    continue;
                }
                //如果 当天 不在设置的星期范围内，跳过
                if (!ps.getDayOfWeeksAsSet().contains(dayOfWeek)) {
                    continue;
                }

                //取出该房型下的时间安排
                Set<Integer> startTimeArray = ps.getStartTimesAsSet();
                for (Integer startTime : startTimeArray) {
                    KtvTaobaoSaleProperty property = new KtvTaobaoSaleProperty();
                    property.goods = productGoods.goods;
                    property.roomType = ps.roomType.getTaobaoId();
                    property.date = dateFormat.format(day);
                    property.price = ps.price;
                    property.quantity = shopPriceSchedule.roomCount;

                    //排除掉已预订的房间所占用的数量
                    for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
                        if (orderInfo.scheduledTime < (startTime + productGoods.product.duration)
                                && (orderInfo.scheduledTime + orderInfo.duration) > startTime) {
                            property.quantity -= 1;
                        }
                    }
                    //如果预订满了，就不再有此SKU
                    if (property.quantity <= 0) {
                        continue;
                    }

                    property.timeRange = startTime + "点至" + (startTime + productGoods.product.duration - 1) + "点";
                    property.createdAt = new Date();
                    property.save();
                    saleProperties.add(property);
                }
            }
        }
        return saleProperties;
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

    public static void showEdit(Long id) {
    }


    public static void update(Long id, @Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
    }

    public static void delete(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        if (priceSchedule == null) {
            index(null, null);
            return;
        }
        priceSchedule.delete();

        index(null, null);
    }
}
