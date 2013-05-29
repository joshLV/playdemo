package models.ktv;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.request.ItemSkuDeleteRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.taobao.api.response.ItemSkuDeleteResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-4-27
 * Time: 下午1:42
 */
public class KtvTaobaoUtil {
    // 淘宝电子凭证的secret
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");


    /**
     * 根据某一价格策略，更新相应门店的所有相应商品的淘宝SKU.
     * 当新建/修改/删除价格策略时，应该调用此方法.
     *
     * @param priceScheduleId 价格策略ID
     */
    public static void updateTaobaoSkuByPriceSchedule(Long priceScheduleId) {
        Logger.info("KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule method start>>>priceScheduleId:" + priceScheduleId);
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(priceScheduleId);
        Logger.info("priceSchedule:" + priceSchedule);
        //找出该价格策略影响的所有门店
        List<KtvShopPriceSchedule> shopPriceSchedules = KtvShopPriceSchedule.find("bySchedule", priceSchedule).fetch();
        List<Shop> affectedShops = new ArrayList<>();
        for (KtvShopPriceSchedule schedule : shopPriceSchedules) {
            affectedShops.add(schedule.shop);
        }

        //找出该价格策略所影响的所有商品
        Query query = JPA.em().createQuery("select p from KtvProductGoods p where p.product = :product and p.shop in :shops");
        query.setParameter("product", priceSchedule.product);
        query.setParameter("shops", affectedShops);

        List<KtvProductGoods> affectedGoodsList = query.getResultList();
        for (KtvProductGoods productGoods : affectedGoodsList) {
            //更新商品在淘宝上的SKU信息
            updateTaobaoSkuByProductGoods(productGoods);
        }

    }

    /**
     * 根据某一商品，更新其在淘宝上的SKU信息.
     *
     * @param productGoods KTV商品.
     */
    public static void updateTaobaoSkuByProductGoods(KtvProductGoods productGoods) {
        Logger.info("KtvTaobaoUtil.updateTaobaoSkuByProductGoodse method start>>>priceSchedule:" + productGoods);
        //构建新的淘宝SKU列表
        List<KtvTaobaoSku> newTaobaoSkuList = skuMapToList(buildTaobaoSku(productGoods.shop, productGoods.product, true), false);
        //从数据库中查出目前的淘宝SKU列表
        List<KtvTaobaoSku> oldTaobaoSkuList = KtvTaobaoSku.find("byGoods", productGoods.goods).fetch();
        //比较两者，得出三个列表，分别是：1、应该添加到淘宝的SKU列表；2、应该更新的淘宝SKU列表；3、应该删除的淘宝SKU列表
        Map<String, List<KtvTaobaoSku>> diffResult = diffTaobaoSku(newTaobaoSkuList, oldTaobaoSkuList);
        //更新该ktv产品所对应的每一个分销渠道上的商品
        List<ResalerProduct> resalerProductList = ResalerProduct.find("byGoodsAndPartner", productGoods.goods, OuterOrderPartner.TB).fetch();
        for (ResalerProduct resalerProduct : resalerProductList) {
            if (resalerProduct.resaler == null) {
                Logger.info("ktv update sku时,resalerProduct.partnerProductId=%s的resaler.id is null,will not update this resalerProduct!", resalerProduct.partnerProductId);
                continue;
            }
            TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resalerProduct.resaler.taobaoCouponAppKey,
                    resalerProduct.resaler.taobaoCouponAppSecretKey);
            //找到淘宝的token
            OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);

            if (StringUtils.isBlank(resalerProduct.partnerProductId)) {
                continue;
            }

            String[] keys = new String[]{"delete", "update", "add"};//先删，后更新，再添加
            for (String key : keys) {
                List<KtvTaobaoSku> skuList = diffResult.get(key);
                switch (key) {
                    case "add":
                        for (KtvTaobaoSku p : skuList) {
                            addSaleSkuOnTaobao(taobaoClient, token, resalerProduct, p);
                        }
                        break;
                    case "update":
                        for (KtvTaobaoSku p : skuList) {
                            updateSaleSkuOnTaobao(taobaoClient, token, resalerProduct, p);
                        }
                        break;
                    case "delete":
                        for (KtvTaobaoSku p : skuList) {
                            deleteSaleSkuOnTaobao(taobaoClient, token, resalerProduct, p);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 构建新的淘宝SKU列表.
     * 如果传入的 shop 和 product 有对应的 KtvProductGoods，那么返回的sku中就有goods，
     * 否则就没有,SKU中只有日期、时间价格和数量这几个信息，没有 goods 信息
     *
     * @param shop      门店
     * @param product   KTV产品
     * @param isPerfect 是否构建 Perfect Tree
     *                  如果该选项为真，则表明该树是 [Perfect Tree] (https://en.wikipedia.org/wiki/Binary_tree)
     *                  依照淘宝，对于无价格/数量信息的叶子节点，我们将其价格设置为最低，数量设置为0
     *                  <p/>
     *                  如果该选项为假，则对于无价格/数量信息的叶子节点，将忽略之，不加入到Map中
     * @return 新的淘宝SKU列表（未 save 到数据库）
     */
    public static Map<String, Map<String, Map<String, KtvTaobaoSku>>> buildTaobaoSku(Shop shop, KtvProduct product, boolean isPerfect) {
        List<KtvTaobaoSku> taobaoSkuList = new ArrayList<>();//结果集

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        //当天18点到24点之后sku不更新，并删除
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 18) {
            today = DateUtils.addDays(today, 1);
        }

        int maxSkuCount = 600;
        int maxDateCount = 10;

        //查出与该KTV商品有关联的、today(包含)之后的所有价格
        Query query = JPA.em().createQuery("select s.schedule from KtvShopPriceSchedule s where s.shop = :shop "
                + "and s.schedule.product = :product and s.schedule.endDay >= :today and s.schedule.deleted = :deleted");


        query.setParameter("shop", shop);
        query.setParameter("product", product);
        query.setParameter("today", today);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        List<KtvPriceSchedule> priceScheduleList = query.getResultList();


        //查出该门店的该产品今天已经卖出、或者被锁定的房间信息
        KtvProductGoods productGoods = KtvProductGoods.find("byShopAndProduct", shop, product).first();
        List<KtvRoomOrderInfo> roomOrderInfoList;
        Goods goods = null;
        if (productGoods == null) {
            roomOrderInfoList = new ArrayList<>();
        } else {
            goods = productGoods.goods;
            roomOrderInfoList = KtvRoomOrderInfo.findScheduled(today, productGoods);
        }
        //处理从今天开始往后的14天内，每一天的sku
        for (int i = 0; i < 14; i++) {
            Date day = DateUtils.addDays(today, i);
            //抓出所有相关的价格策略，以日期范围 和 星期 为条件，筛选出合适的，然后进一步处理
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

                //取出该门店在该价格策略上设置的房间数
                KtvShopPriceSchedule shopPriceSchedule = KtvShopPriceSchedule.find("byShopAndSchedule", shop, ps).first();

                //取出该房型下的时间安排
                Set<Integer> startTimeArray = ps.getStartTimesAsSet();
                for (Integer startTime : startTimeArray) {
                    KtvTaobaoSku sku = new KtvTaobaoSku();
                    sku.goods = goods;
                    sku.setRoomType(ps.roomType.getTaobaoId());
                    sku.setDate(dateFormat.format(day));
                    sku.price = ps.price;
                    sku.quantity = shopPriceSchedule.roomCount;

                    //排除掉已预订的房间所占用的数量
                    for (KtvRoomOrderInfo orderInfo : roomOrderInfoList) {
                        if (orderInfo.scheduledDay.compareTo(day) != 0) {
                            continue;
                        }
                        if (orderInfo.duration != product.duration) {
                            continue;
                        }
                        if (orderInfo.scheduledTime < (startTime + product.duration)
                                && (orderInfo.scheduledTime + orderInfo.duration) > startTime) {
                            sku.quantity -= 1;
                        }
                    }
                    //如果预订满了，就不再有此SKU
                    if (sku.quantity <= 0) {
                        continue;
                    }

                    sku.setTimeRange(startTime + "点至" + (startTime + product.duration) + "点");
                    sku.createdAt = new Date();
                    taobaoSkuList.add(sku);
                }
            }
        }
        return skuListToMap(taobaoSkuList, goods, isPerfect);
    }

    /**
     * 返回 以 包厢 -》 日期 -》 时长 三级形式的 树状 sku map
     *
     * @param perfect 是否构建 Perfect Tree
     *                如果该选项为真，则表明该树是 [Perfect Tree] (https://en.wikipedia.org/wiki/Binary_tree)
     *                依照淘宝，对于无价格/数量信息的叶子节点，我们将其价格设置为最低，数量设置为0
     *                <p/>
     *                如果该选项为假，则对于无价格/数量信息的叶子节点，将忽略之，不加入到Map中
     *                <p/>
     *                (发布到淘宝后台，是让淘宝后台以包厢-》时长-》日期 的形式显示的，不影响逻辑。以上面所说的三级形保存，是为了方便在我方页面显示相应信息)
     */
    public static Map<String, Map<String, Map<String, KtvTaobaoSku>>> skuListToMap(List<KtvTaobaoSku> skuList, Goods goods, boolean perfect) {
        Map<String, Map<String, Map<String, KtvTaobaoSku>>> result = new HashMap<>();
        for (KtvTaobaoSku sku : skuList) {
            Map<String, Map<String, KtvTaobaoSku>> skuMapByRoomType = result.get(sku.getRoomType());
            if (skuMapByRoomType == null) {
                skuMapByRoomType = new HashMap<>();
                result.put(sku.getRoomType(), skuMapByRoomType);
            }
            Map<String, KtvTaobaoSku> skuMapByDate = skuMapByRoomType.get(sku.getDate());
            if (skuMapByDate == null) {
                skuMapByDate = new HashMap<>();
                skuMapByRoomType.put(sku.getDate(), skuMapByDate);
            }
            skuMapByDate.put(sku.getTimeRange(), sku);
        }
        if (!perfect) {
            return result;
        }

        //构建 Perfect Tree

        Set<String> roomTypeSet = new HashSet<>();
        Set<String> dateSet = new HashSet<>();
        Set<String> timeRangeSet = new HashSet<>();
        BigDecimal price = null;//最低价
        for (KtvTaobaoSku sku : skuList) {
            if (sku.quantity == 0) {
                continue;
            }
            roomTypeSet.add(sku.getRoomType());
            dateSet.add(sku.getDate());
            timeRangeSet.add(sku.getTimeRange());
            if (price == null) {
                price = sku.price;
            } else if (price.compareTo(sku.price) > 0) {
                price = sku.price;
            }

        }
        for (String roomType : roomTypeSet) {
            for (String date : dateSet) {
                for (String timeRange : timeRangeSet) {
                    Map<String, Map<String, KtvTaobaoSku>> skuMapByRoomType = result.get(roomType);
                    if (skuMapByRoomType == null) {
                        skuMapByRoomType = new HashMap<>();
                        result.put(roomType, skuMapByRoomType);
                    }
                    Map<String, KtvTaobaoSku> skuMapByDate = skuMapByRoomType.get(date);
                    if (skuMapByDate == null) {
                        skuMapByDate = new HashMap<>();
                        skuMapByRoomType.put(date, skuMapByDate);
                    }
                    if (skuMapByDate.get(timeRange) == null) {
                        //填充
                        KtvTaobaoSku sku = new KtvTaobaoSku();
                        sku.goods = goods;
                        sku.setRoomType(roomType);
                        sku.setDate(date);
                        sku.price = price;
                        sku.quantity = 0;
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param skuMap sku 树
     * @param reduce 是否输出简化列表
     *               如果该选项为真，则输出时剔除数量为0的SKU
     *               如果该选项为假，则完成输出map中的所有sku
     * @return sku 列表
     */
    public static List<KtvTaobaoSku> skuMapToList(Map<String, Map<String, Map<String, KtvTaobaoSku>>> skuMap, boolean reduce) {
        List<KtvTaobaoSku> skuList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, KtvTaobaoSku>>> entryA : skuMap.entrySet()) {
            for (Map.Entry<String, Map<String, KtvTaobaoSku>> entryB : entryA.getValue().entrySet()) {
                for (Map.Entry<String, KtvTaobaoSku> entryC : entryB.getValue().entrySet()) {
                    KtvTaobaoSku sku = entryC.getValue();
                    if (sku.quantity == 0 && reduce) {
                        continue;
                    }
                    skuList.add(sku);
                }
            }
        }
        return skuList;
    }


    /**
     * 在淘宝上删除一个SKU
     */
    private static void deleteSaleSkuOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSku p) {
        ItemSkuDeleteRequest req = new ItemSkuDeleteRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));

        req.setProperties(p.getProperties());
        Logger.info(req.getProperties());


        try {
            ItemSkuDeleteResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode())) {
                p.delete();
            }
        } catch (ApiException e) {
            Logger.info(e, "delete sku to taobao failed");
        }
    }

    /**
     * 在淘宝上更新一个SKU
     */
    private static void updateSaleSkuOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSku p) {
        ItemSkuUpdateRequest req = new ItemSkuUpdateRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));

        req.setProperties(p.getProperties());
        req.setQuantity((long) p.quantity);
        req.setPrice(p.price.toString());

        try {
            ItemSkuUpdateResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode())) {
                p.save();
            }
        } catch (ApiException e) {
            Logger.info(e, "update sku to taobao failed");
        }
    }

    /**
     * 在淘宝上添加一个SKU
     */
    private static void addSaleSkuOnTaobao(TaobaoClient taobaoClient, OAuthToken token, ResalerProduct resalerProduct, KtvTaobaoSku p) {
        ItemSkuAddRequest req = new ItemSkuAddRequest();
        req.setNumIid(Long.valueOf(resalerProduct.partnerProductId));
        req.setProperties(p.getProperties());
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


    /**
     * 比较两组SKU，得出三个列表，分别是：1、应该添加到淘宝的SKU列表；2、应该更新的淘宝SKU列表；3、应该删除的淘宝SKU列表
     * 以后者(oldSkuList)为基准【后者在此处一般指数据库中的数据】)
     */
    public static Map<String, List<KtvTaobaoSku>> diffTaobaoSku(
            List<KtvTaobaoSku> newSkuList, List<KtvTaobaoSku> oldSkuList) {

        Map<String, List<KtvTaobaoSku>> result = new HashMap<>();
        //应该更新的列表
        List<KtvTaobaoSku> tobeUpdated = new ArrayList<>();
        //应该添加的列表
        List<KtvTaobaoSku> tobeAdded = new ArrayList<>();
        //应该删除的列表。为了不影响传入的列表，此处浅拷贝一份出来
        Map<String, KtvTaobaoSku> tobeDeleted = new HashMap<>(oldSkuList.size());

        Set<String> roomTypeSet = new HashSet<>();
        Set<String> dateSet = new HashSet<>();
        Set<String> timeRangeSet = new HashSet<>();
        for (KtvTaobaoSku oldSaleProperty : oldSkuList) {
            tobeDeleted.put(oldSaleProperty.getProperties(), oldSaleProperty);
            roomTypeSet.add(oldSaleProperty.getRoomType());
            dateSet.add(oldSaleProperty.getDate());
            timeRangeSet.add(oldSaleProperty.getTimeRange());
        }

        for (KtvTaobaoSku newProperty : newSkuList) {
            boolean found = false;
            for (Map.Entry<String, KtvTaobaoSku> entry : tobeDeleted.entrySet()) {
                //如果两者的销售属性相同（包厢、日期、时间段），则不是更新，就是忽略
                if (newProperty.getProperties().equals(entry.getValue().getProperties())) {
                    if (newProperty.quantity != 0) {
                        //从待删除中去除
                        KtvTaobaoSku oldProperty = tobeDeleted.remove(entry.getKey());
                        //如果数量或者价格有所不同，则添加到待更新的列表里，否则即是忽略
                        if (!oldProperty.quantity.equals(newProperty.quantity)
                                || oldProperty.price.compareTo(newProperty.price) != 0) {
                            oldProperty.quantity = newProperty.quantity;
                            oldProperty.price = newProperty.price;

                            //oldProperty摇身一变，成为了待更新的SKU
                            tobeUpdated.add(oldProperty);
                        }//else ignore
                    } else {
                        entry.getValue().quantity = newProperty.quantity;
                    }
                    found = true;
                    break;
                }
            }
            //没有相同的，就是新值，添加到tobeAdded
            if (!found) {
                tobeAdded.add(newProperty);
            }
        }

        //将待删除的再重新筛选一遍，只有可以完整删除某一销售属性时，才删除那个销售属性下的所有SKU。剩余的回归到tobeUpdated列表中去（quantity已设为0）
        Map<String, Map<String, Map<String, KtvTaobaoSku>>> tobeDeletedMap =
                skuListToMap(new ArrayList<>(tobeDeleted.values()), null, false);
        Map<String, KtvTaobaoSku> realDeletedMap = new HashMap<>();
        Set<String> tobeDeletedProperties = new HashSet<>(tobeDeleted.keySet());

        //尝试删除同一日期下的所有欢唱时间
        for (String roomType : roomTypeSet) {
            for (String date : dateSet) {
                Set<String> properties = new HashSet<>();
                for (String timeRange : timeRangeSet) {
                    properties.add(KtvTaobaoSku.buildProperties(roomType, timeRange, date));
                }
                if (tobeDeletedProperties.containsAll(properties)) {
                    for (String timeRange : timeRangeSet) {
                        KtvTaobaoSku sku = tobeDeletedMap.get(roomType).get(date).get(timeRange);
                        realDeletedMap.put(sku.getProperties(), sku);
                        tobeDeleted.remove(sku.getProperties());
                    }
                }
            }
        }
        //尝试删除同一 欢唱时间下的 所有日期
        for (String roomType : roomTypeSet) {
            for (String timeRange : timeRangeSet) {
                Set<String> properties = new HashSet<>();
                for (String date : dateSet) {
                    properties.add(KtvTaobaoSku.buildProperties(roomType, timeRange, date));
                }
                if (tobeDeletedProperties.containsAll(properties)) {
                    for (String date : dateSet) {
                        KtvTaobaoSku sku = tobeDeletedMap.get(roomType).get(date).get(timeRange);
                        realDeletedMap.put(sku.getProperties(), sku);
                        tobeDeleted.remove(sku.getProperties());
                    }
                }
            }
        }

        tobeUpdated.addAll(tobeDeleted.values());
        tobeDeleted = realDeletedMap;

        result.put("update", tobeUpdated);
        result.put("add", tobeAdded);
        result.put("delete", new ArrayList(tobeDeleted.values()));
        return result;
    }
}
