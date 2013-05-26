package models.ktv;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.*;
import com.taobao.api.response.*;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;

import javax.persistence.Query;
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
        List<KtvTaobaoSku> newTaobaoSkuList = buildTaobaoSku(productGoods.shop, productGoods.product);
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

            for (Map.Entry<String, List<KtvTaobaoSku>> entry : diffResult.entrySet()) {
                switch (entry.getKey()) {
                    case "add":
                        for (KtvTaobaoSku p : entry.getValue()) {
                            addSaleSkuOnTaobao(taobaoClient, token, resalerProduct, p);
                        }
                        break;
                    case "update":
                        for (KtvTaobaoSku p : entry.getValue()) {
                            updateSaleSkuOnTaobao(taobaoClient, token, resalerProduct, p);
                        }
                        break;
                    case "delete":
                        for (KtvTaobaoSku p : entry.getValue()) {
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
     * 否则就没有,只有SKU的日期、时间价格和数量这几个信息
     *
     * @param shop 门店
     * @param product KTV产品
     * @return 新的淘宝SKU列表
     */
    public static List<KtvTaobaoSku> buildTaobaoSku(Shop shop, KtvProduct product) {
        List<KtvTaobaoSku> taobaoSkuList = new ArrayList<>();//结果集

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);

        //当天18点到24点之后sku不更新，并删除
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 18) {
            today = DateUtils.addDays(today, 1);
        }

        //查出与该KTV商品有关联的、最近7天的所有价格策略
        Query query = JPA.em().createQuery("select s.schedule from KtvShopPriceSchedule s where s.shop = :shop "
                + "and s.schedule.product = :product and s.schedule.startDay <= :endDay and s.schedule.endDay >= :startDay and s.schedule.deleted = :deleted");


        Date endDay = DateUtils.addDays(today, 6);

        query.setParameter("shop", shop);
        query.setParameter("product", product);
        query.setParameter("startDay", today);
        query.setParameter("endDay", endDay);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        List<KtvPriceSchedule> priceScheduleList = query.getResultList();


        //查出该门店的该产品今天已经卖出、或者被锁定的房间信息
        KtvProductGoods productGoods = KtvProductGoods.find("byShopAndProduct", shop, product).first();
        List<KtvRoomOrderInfo> roomOrderInfoList;
        Goods goods = null;
        if (productGoods == null) {
            roomOrderInfoList = new ArrayList<>();
        }else {
            goods = productGoods.goods;
            roomOrderInfoList = KtvRoomOrderInfo.findScheduled(today, productGoods);
        }
        //处理从今天开始往后的7天内，每一天的sku
        for (int i = 0; i < 7; i++) {
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
        return taobaoSkuList;

    }

    /**
     * 返回 以 包厢 -》 日期 -》 SKU 三级形式的 Map
     *
     * @param skuList
     * @return
     */
    public static Map<String, Map<String, List<KtvTaobaoSku>>> taobaoSkuListToMap(List<KtvTaobaoSku> skuList) {
        Map<String, Map<String, List<KtvTaobaoSku>>> result = new HashMap<>();
        for (KtvTaobaoSku sku : skuList) {
            Map<String, List<KtvTaobaoSku>> skuListByRoomType = result.get(sku.getRoomType());
            if (skuListByRoomType == null) {
                skuListByRoomType = new HashMap<>();
                result.put(sku.getRoomType(), skuListByRoomType);
            }
            List<KtvTaobaoSku> skuListByDate = skuListByRoomType.get(sku.getDate());
            if (skuListByDate == null) {
                skuListByDate = new ArrayList<>();
                skuListByRoomType.put(sku.getDate(), skuListByDate);
            }
            skuListByDate.add(sku);
        }
        return result;
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
        List<KtvTaobaoSku> tobeDeleted = new ArrayList<>(oldSkuList.size());
        for (KtvTaobaoSku oldSaleProperty : oldSkuList) {
            tobeDeleted.add(oldSaleProperty);
        }

        for (KtvTaobaoSku newProperty : newSkuList) {
            boolean found = false;
            for (int i = 0; i < tobeDeleted.size(); i++) {
                //如果两者的销售属性相同（包厢、日期、时间段），则不是更新，就是忽略
                if (newProperty.getProperties().equals(tobeDeleted.get(i).getProperties())) {
                    //从待删除中去除
                    KtvTaobaoSku oldProperty = tobeDeleted.remove(i);
                    //如果数量或者价格有所不同，则添加到待更新的列表里，否则即是忽略
                    if (!oldProperty.quantity.equals(newProperty.quantity)
                            || oldProperty.price.compareTo(newProperty.price) != 0) {
                        oldProperty.quantity = newProperty.quantity;
                        oldProperty.price = newProperty.price;

                        //oldProperty摇身一变，成为了待更新的SKU
                        tobeUpdated.add(oldProperty);
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
        result.put("update", tobeUpdated);
        result.put("add", tobeAdded);
        result.put("delete", tobeDeleted);
        return result;
    }
}
