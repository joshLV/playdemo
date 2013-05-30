package controllers.resale;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItempropsGetResponse;
import com.uhuila.common.constants.DeletedStatus;
import controllers.OperateRbac;
import models.accounts.AccountType;
import models.admin.SupplierUser;
import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.*;
import models.supplier.Supplier;
import models.taobao.TaobaoCouponUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author yanjy
 */
@With(OperateRbac.class)
@ActiveNavigation("ktv_taobao_product")
public class KtvTaobaoProducts extends Controller {

    @ActiveNavigation("ktv_taobao_product")
    public static void index(Long supplierId) {
        List<Supplier> supplierList = Supplier.find("select p.supplier from SupplierProperty p where p.name=? and" +
                " p.value = 1 and p.supplier.deleted=?", Supplier.KTV_SUPPLIER, DeletedStatus.UN_DELETED).fetch();
        if (supplierId != null) {
            List<Shop> shops = Shop.findShopBySupplier(supplierId);
            List<KtvProduct> products = KtvProduct.findProductBySupplier(supplierId);

            renderArgs.put("supplierId", supplierId);
            renderArgs.put("shops", shops);
            renderArgs.put("products", products);
        }
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        render(supplierList,resaler);
    }


    @ActiveNavigation("ktv_taobao_product")
    public static void showPublish(Shop shop, KtvProduct product) {
        if (shop == null || product == null) {
            renderText("传入的参数有误，请检查");
            return;
        }

        SortedMap<String, SortedMap<Date, SortedMap<Integer, KtvTaobaoSku>>> taobaoSkuMap = KtvTaobaoUtil.buildTaobaoSku(shop, product, false);

        if (taobaoSkuMap.size() == 0) {
            render("resale/KtvTaobaoProducts/noSku.html", shop, product);
        }

        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        TaobaoClient client = new DefaultTaobaoClient(TaobaoCouponUtil.URL, resaler.taobaoCouponAppKey, resaler.taobaoCouponAppSecretKey);
        ItempropsGetRequest req = new ItempropsGetRequest();
        req.setCid(KtvTaobaoUtil.defaultCid);
        ItempropsGetResponse response;
        try {
            response = client.execute(req);
        } catch (ApiException e) {
            renderText("淘宝接口调用出错，请稍后再试");
            return;
        }
        String props = new Gson().toJson(response.getItemProps());


        render(shop, product, props, taobaoSkuMap, resaler);
    }

    public static void publish(Shop shop, KtvProduct product, String title, String prodImg, String desc,
                               boolean onsale, String locationProvince, String locationCity, String ktvBrandPid,
                               String ktvBrand, String ktvProvincePid, String[] ktvProvinces, long cid,
                               String ktvCityPid, String[] ktvCities, String expiryDate, String faceValuePid) {

        List<KtvTaobaoSku> taobaoSkuList = KtvTaobaoUtil.skuMapToList(KtvTaobaoUtil.buildTaobaoSku(shop, product, true), false);
        if (taobaoSkuList.size() == 0) {
            render("resale/KtvTaobaoProducts/noSku.html", shop, product);
        }

        ItemAddRequest request = new ItemAddRequest();

        request.setTitle(title);//设置标题
//        request.setPicPath(prodImg);//设置主图
        request.setDesc(desc);//设置商品描述
        request.setLocationState(locationProvince);//设置宝贝所在省份
        request.setLocationCity(locationCity);//设置宝贝所在城市
        request.setLocalityLifeOnsaleAutoRefundRatio(100L);//自动退款比例为100%
        request.setLocalityLifeExpirydate(expiryDate);//30天内有效
        request.setCid(cid);//设置类目为休闲娱乐-》KTV
        request.setType("fixed");//一口价
        request.setStuffStatus("stuff_status");//全新
        if (onsale) {
            request.setApproveStatus("onsale");
        } else {
            request.setApproveStatus("instock");
        }

        //准备属性列表
        StringBuilder props = new StringBuilder();
        props.append(ktvBrandPid).append(":").append(ktvBrand).append(";");//设置商品属性：品牌
        for (String ktvProvince : ktvProvinces) {
            props.append(ktvProvincePid).append(":").append(ktvProvince).append(";"); //设置商品属性：适用省份
        }
        for (String ktvCity : ktvCities) {
            props.append(ktvCityPid).append(":").append(ktvCity).append(";"); //设置商品属性：适用城市
        }

        //找到默认的分销商
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        //根据已知信息，创建一个符合KTV需求的商品
        Goods goods = autoCreateGoods(shop, product);
        //根据此商品,创建一个resalerProduct
        ResalerProduct resalerProduct = ResalerProduct.alloc(OuterOrderPartner.TB, resaler, goods);
        //设置外部商品ID
        request.setOuterId(String.valueOf(resalerProduct.goodsLinkId));


        //------- 准备SKU ----
        List<String> skuProperties = new ArrayList<>();
        List<String> skuQuantities = new ArrayList<>();
        List<String> skuPrices = new ArrayList<>();
        List<String> skuOuterIds = new ArrayList<>();

        Long num = 0L;//商品数量
        BigDecimal minPrice = null;//设置商品价格为所有价格中最小的那一个
        BigDecimal maxPrice = BigDecimal.ZERO;//设置面值为所有的价格之中最大的那一个，还要再乘以1.5并取整
        Set<String> propSet = new HashSet<>();//房间类型因为是KTV类目的已有销售属性，因此要加入到props里（我们的日期和欢唱时间属于自定销售属性）
        for (KtvTaobaoSku taobaoSku : taobaoSkuList) {
            skuProperties.add(taobaoSku.getProperties());//添加SKU 属性
            skuQuantities.add(taobaoSku.quantity.toString());//添加SKU数量
            skuPrices.add(taobaoSku.price.toString());//添加SKU价格
            skuOuterIds.add("");//添加SKU外部商品ID(我们不为每个SKU设置单独的SKU，但是参数中还是要传入空值)
            num += taobaoSku.quantity;
            propSet.add(taobaoSku.getRoomType());
            if (minPrice == null) {
                minPrice = taobaoSku.price;
            } else if (taobaoSku.price.compareTo(minPrice) < 0) {
                minPrice = taobaoSku.price;
            }
            if (taobaoSku.price.compareTo(maxPrice) > 0) {
                maxPrice = taobaoSku.price;
            }
        }
        props.append(StringUtils.join(propSet, ";")).append(";");
        request.setNum(num);//数量
        request.setPrice(minPrice.toString());//设置价格为最低价

        request.setProps(props.toString());
        request.setSkuProperties(StringUtils.join(skuProperties, ","));
        request.setSkuQuantities(StringUtils.join(skuQuantities, ","));
        request.setSkuPrices(StringUtils.join(skuPrices, ","));
        request.setSkuOuterIds(StringUtils.join(skuOuterIds, ","));

        //------ 准备自定义属性---
        List<String> inputPids = new ArrayList<>();
        List<String> inputStrs = new ArrayList<>();
        //面值
        inputPids.add(faceValuePid);
        inputStrs.add(maxPrice.multiply(new BigDecimal(1.5)).setScale(0, BigDecimal.ROUND_DOWN).toString());

        request.setInputPids(StringUtils.join(inputPids, ","));
        request.setInputStr(StringUtils.join(inputStrs, ","));

        request.setProps(props.toString());//设置商品属性


        //执行发布
        ItemAddResponse response;
        TaobaoClient client = new DefaultTaobaoClient(TaobaoCouponUtil.URL, resaler.taobaoCouponAppKey, resaler.taobaoCouponAppSecretKey);
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        try {
            response = client.execute(request, token.accessToken);
        } catch (ApiException e) {
            String error = e.getErrCode() + ":" + e.getErrMsg();
            render("resale/KtvTaobaoProducts/publishResult.html", error, shop, product);
            return;
        }
        if (StringUtils.isNotBlank(response.getErrorCode())) {
            String error = response.getErrorCode() + ":" + response.getMsg() + "," + response.getSubCode() + ":" + response.getSubMsg();
            render("resale/KtvTaobaoProducts/publishResult.html", error, shop, product);
            return;
        }
        //保存淘宝的商品ID
        String taobaoProductId = String.valueOf(response.getItem().getNumIid());
        resalerProduct.creator(OperateRbac.currentUser().id).status(ResalerProductStatus.UPLOADED).save();
        resalerProduct.partnerProduct(taobaoProductId);
        resalerProduct.save();

        //创建 ktvProductGoods
        KtvProductGoods ktvProductGoods = new KtvProductGoods();
        ktvProductGoods.goods = goods;
        ktvProductGoods.product = product;
        ktvProductGoods.shop = shop;
        ktvProductGoods.save();

        for (KtvTaobaoSku sku : taobaoSkuList) {
            sku.goods = goods;
            sku.save();//将SKU信息保存到数据库
        }

        render("resale/KtvTaobaoProducts/publishResult.html", taobaoProductId, shop, product);
    }

    public static Goods autoCreateGoods(Shop shop, KtvProduct product) {
        Goods goods = new Goods();
        goods.shops = new HashSet<>();
        goods.shops.add(shop);
        goods.materialType = MaterialType.ELECTRONIC;
        goods.status = GoodsStatus.ONSALE;
        goods.name = shop.name + product.name;
        goods.shortName = goods.name;
        goods.title = goods.name;
        goods.setDetails(goods.name);
        goods.setPrompt(goods.name);
        goods.setExhibition(goods.name);
        goods.product = product;
        goods.isAllShop = false;
        goods.faceValue = BigDecimal.ONE;
        goods.cumulativeStocks = 9999L;
        goods.supplierId = product.supplier.id;
        goods.salePrice = BigDecimal.ONE;
        goods.originalPrice = BigDecimal.ONE;
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.lockVersion = 0;
        goods.resetCode();
        return goods.save();
    }
}
