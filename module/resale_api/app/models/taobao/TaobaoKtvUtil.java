package models.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import models.accounts.AccountType;
import models.admin.SupplierUser;
import models.ktv.KtvPriceSchedule;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.Order;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import play.Logger;
import play.Play;

/**
 * User: yan
 * Date: 13-4-27
 * Time: 下午1:42
 */
public class TaobaoKtvUtil {
    // 淘宝电子凭证的secret
    public static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    public static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");
    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");

    /**
     * 设置价格策略时推送商品并且设置sku信息
     */
    public static void addGoodsSku(KtvPriceSchedule schedule, SupplierUser supplierUser) {
        if (!"1".equals(supplierUser.supplier.getProperty(Supplier.KTV_SUPPLIER))) {
            return;
        }
//        for (Shop shop : schedule.shops) {
        Shop shop = Shop.findById(2438L);
        Goods goods = Goods.findKtvGood(supplierUser.supplier, shop);
        if (goods == null) {
            return;
        }

        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.TB, resaler, goods);

//        String property = setSkuProperty(schedule);
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
        //找到淘宝的token

        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(100l);// 商品数量
        addRequest.setPrice("10.0");
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(goods.title + "---时段3小时");
        addRequest.setDesc(goods.getPrompt());
        addRequest.setLocationState("浙江");
        addRequest.setLocationCity("杭州");
        addRequest.setCid(50019081L);//休闲娱乐 KTV
        //类别：品牌：城市
        addRequest.setProps("8648373:29412;8648185:30510;27426219:6312905;27426219:3442354;27426219:6769368;27426219:3374388;27426219:40867986;27426219:10122;");
        addRequest.setInputStr("200"); //面值value
        addRequest.setInputPids("5392163");//面值key
        addRequest.setApproveStatus("instock");//初始为下架的，在淘宝仓库中
        addRequest.setOuterId(String.valueOf(product.goodsLinkId));
        addRequest.setSellerCids("510052569");

        addRequest.setSkuProperties("$时间:9：00至12：00;");
        addRequest.setSkuQuantities("4");
        addRequest.setSkuPrices("10");
        addRequest.setSkuOuterIds(String.valueOf(product.goodsLinkId));
        try {
            ItemAddResponse addResponse = taobaoClient.execute(addRequest, token.accessToken);
            if (addResponse != null && addResponse.getErrorCode() == null) {
                Long taobaoItemId = addResponse.getItem().getNumIid();
                //保存商品状态
                product.creator(supplierUser.id).partnerProduct(String.valueOf(taobaoItemId))
                        .status(ResalerProductStatus.UPLOADED).save();
            }
        } catch (ApiException e) {
            Logger.info(e, "add item sku to taobao failed");
        } catch (Exception e) {
            Logger.info(e, "add item sku to taobao failed");
        }

//    }

    }

    /**
     * 根据商品ID更新sku信息,这样更新是追加一个新的sku
     */
    public static void updateSku(KtvPriceSchedule schedule, SupplierUser supplierUser) {
        Shop shop = Shop.findById(2438L);
        Goods goods = Goods.findKtvGood(supplierUser.supplier, shop);
        if (goods == null) {
            return;
        }
//        ResalerProduct product = ResalerProduct.getGoodsByShop(shop, supplierUser.supplier);
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
        //找到淘宝的token
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        ItemSkuUpdateRequest req = new ItemSkuUpdateRequest();
//        req.setNumIid(Long.valueOf(product.partnerProductId));
        req.setProperties("$时间:12：00至15：00;");
        req.setQuantity(3L);
        req.setPrice("9");
        req.setOuterId("123456");
        try {
            ItemSkuUpdateResponse response = taobaoClient.execute(req, token.accessToken);
        } catch (ApiException e) {
            Logger.info(e, "update ktv sku to taobao failed");
        }

    }


    /**
     * 下单更新sku库存信息
     */
    public static void updateSkuInventory(Order order) {

    }

    public static String setSkuProperty(KtvPriceSchedule schedule) {
        String properties = "";
        //pid:vid;pid2:vid2;$pText:vText

        return properties;
    }


}
