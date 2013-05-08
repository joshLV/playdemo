package models.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import com.uhuila.common.constants.DeletedStatus;
import models.RabbitMQConsumerWithTx;
import models.accounts.AccountType;
import models.ktv.*;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:18
 */
@OnApplicationStart(async = true)
public class KtvSkuConsumer extends RabbitMQConsumerWithTx<KtvSkuMessage> {
    // 淘宝电子凭证的secret
    public static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    public static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");

    @Override
    public void consumeWithTx(KtvSkuMessage message) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(message.scheduledId);
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


    /**
     * 请求taobao删除商品对应的sku
     */
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

    /**
     * 请求taobao更新商品对应的sku
     */
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

    /**
     * 请求taobao添加商品对应的sku
     */
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


    /**
     * 比较两个淘宝销售属性列表，区分出需新建、需更新、需删除的三个列表.(以后者为基准【后者在此处一般指数据库中的数据】)
     */
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

    /**
     * 根据价格策略，构建ktv商品所有门店的对应策略类型的sku属性信息
     */
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

    @Override
    protected Class getMessageType() {
        return KtvSkuMessage.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.QUEUE_NAME;
    }
}
