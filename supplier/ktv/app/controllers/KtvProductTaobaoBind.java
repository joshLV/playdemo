package controllers;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.request.ItemUpdateRequest;
import com.taobao.api.response.ItemGetResponse;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.accounts.AccountType;
import models.admin.SupplierUser;
import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoUtil;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.*;
import models.supplier.Supplier;
import models.taobao.KtvSkuMessageUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: yan
 * Date: 13-5-23
 * Time: 上午11:59
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvProductTaobaoBind extends Controller {
    public static Pattern taobaoProductIdPattern = Pattern.compile(".*[\\?|&]id=(\\d+).*");

    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");

    /**
     * 产品绑定页面
     */
    public static void index() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        Long supplierId = supplier.id;
        List<Shop> shops = Shop.findShopBySupplier(supplierId);
        List<KtvProduct> products = KtvProduct.findProductBySupplier(supplierId);

        if (supplier.defaultResalerId == null) {
            render("KtvProductTaobaoBind/auth.html");
        }
        Resaler resaler = Resaler.findById(supplier.defaultResalerId);
        if (resaler != null) {
            OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
            if (token == null) {
                render("KtvProductTaobaoBind/auth.html", resaler);
            }
        } else {
            render("KtvProductTaobaoBind/auth.html");
        }


        render(shops, products);
    }

    /**
     * 根据淘宝URL，取得该淘宝的产品名称信息
     *
     * @param url 淘宝URL
     */
    public static void queryTaobaoProduct(String url) {
        Matcher matcher = taobaoProductIdPattern.matcher(url);
        Supplier supplier = SupplierRbac.currentUser().supplier;
        Resaler resaler = Resaler.findById(supplier.defaultResalerId);
        if (matcher.matches()) {
            Long taobaoProductId = Long.parseLong(matcher.group(1));

            TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resaler.taobaoCouponAppKey,
                    resaler.taobaoCouponAppSecretKey);
            //找到淘宝的token
            OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);

            ItemGetRequest req = new ItemGetRequest();
            req.setNumIid(taobaoProductId);
            req.setFields("title");

            try {
                ItemGetResponse response = taobaoClient.execute(req, token.accessToken);
                if (StringUtils.isBlank(response.getErrorCode())) {
                    renderJSON("{\"info\":\"" + response.getItem().getTitle() + "\",\"taobaoProductId\":\"" + taobaoProductId + "\"}");
                } else {
                    renderJSON("{\"error\":\"请求淘宝API失败,错误信息:\"" + response.getMsg() + "\"}");
                }
            } catch (ApiException e) {
                Logger.info(e, "taobao itemget request failed");
                renderJSON("{\"error\":\"taobao itemget request failed!\"}");
            }

        } else {
            renderJSON("{\"error\":\"无效的淘宝URL\"}");
        }

    }

    /**
     * 绑定产品,创建goods信息以及resalerProduct
     */
    public static void bindProduct(Shop shop, KtvProduct product, String taobaoProductId) {
        if (shop.id == null) {
            renderJSON("{\"error\":\"门店信息不存在\"}");
        }
        if (product.id == null) {
            renderJSON("{\"error\":\"产品信息不存在\"}");
        }
        if (StringUtils.isBlank(taobaoProductId)) {
            renderJSON("{\"error\":\"淘宝产品编号不存在\"}");
        }

        KtvProductGoods ktvProductGoods =  KtvProductGoods.find("byShopAndProduct", shop, product).first();

        SupplierUser supplierUser = SupplierRbac.currentUser();
        Resaler resaler = Resaler.findById(supplierUser.supplier.defaultResalerId);
        if (ktvProductGoods == null) {
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
            goods.supplierId = supplierUser.supplier.id;
            goods.salePrice = BigDecimal.ONE;
            goods.originalPrice = BigDecimal.ONE;
            goods.deleted = DeletedStatus.UN_DELETED;
            goods.lockVersion = 0;
            goods.resetCode();
            goods.save();

            ktvProductGoods = new KtvProductGoods();
            ktvProductGoods.goods = goods;
            ktvProductGoods.shop = shop;
            ktvProductGoods.product = product;
            ktvProductGoods.save();

            ResalerProduct resalerProduct = ResalerProduct.alloc(OuterOrderPartner.TB, resaler, goods);
            resalerProduct.partnerProductId = taobaoProductId;
            //保存商品状态
            resalerProduct.creator(supplierUser.id).partnerProduct(taobaoProductId)
                    .status(ResalerProductStatus.UPLOADED).save();

            JPA.em().flush();

            TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, resaler.taobaoCouponAppKey,
                    resaler.taobaoCouponAppSecretKey);

            //找到淘宝的token
            OAuthToken token = OAuthToken.getOAuthToken(resalerProduct.resaler.id, AccountType.RESALER, WebSite.TAOBAO);
            //判断类目是否为KTV
            boolean isKtvPro = checkKtvProperty(taobaoClient, token, resalerProduct.partnerProductId);
            if (!isKtvPro) {
                renderJSON("{\"error\":\"该淘宝商品类目不是KTV，请手动在淘宝后台把此商品类目改为KTV！\"}");
            }

            //更新淘宝SKU
            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(ktvProductGoods);

            //更新淘宝外部编码
            ItemUpdateRequest itemUpdateRequest = new ItemUpdateRequest();
            itemUpdateRequest.setOuterId(String.valueOf(resalerProduct.goodsLinkId));
            itemUpdateRequest.setNumIid(Long.parseLong(taobaoProductId));
            try {
                taobaoClient.execute(itemUpdateRequest, token.accessToken);
            } catch (ApiException e) {
                renderJSON("{\"error\":\"更新淘宝外部商家编码失败,请手动在淘宝后台添加外部编码：" + resalerProduct.goodsLinkId + "\"}");
            }
            renderJSON("{\"info\":\"" + shop.name + product.name + "\",\"taobaoProductId\":\"" + taobaoProductId + "\"}");
        } else {
            Goods goods = Goods.find("select g from Goods g join g.shops s where g.supplierId=? and g.deleted=? and s.id=? and g.status =? " +
                    "and g.product=?", supplierUser.supplier.id, DeletedStatus.UN_DELETED, shop.id, GoodsStatus.ONSALE, product).first();
            if (goods == null) {
                renderJSON("{\"error\":\"没有找到该门店下对应的产品信息！\"}");
            }

            ResalerProduct resalerProduct =  ResalerProduct.find("byGoodsAndResalerAndPartner",
                    goods, resaler, OuterOrderPartner.TB).first();

            if (resalerProduct == null) {
                resalerProduct = ResalerProduct.alloc(OuterOrderPartner.TB, resaler, goods);
                resalerProduct.partnerProductId = taobaoProductId;
                //保存商品状态
                resalerProduct.creator(supplierUser.id).partnerProduct(taobaoProductId)
                        .status(ResalerProductStatus.UPLOADED).save();

                renderJSON("{\"info\":\"" + shop.name + product.name + "\",\"taobaoProductId\":\"" + taobaoProductId + "\"}");
            }
        }
        renderJSON("{\"error\":\"该商品已经绑定过了\"}");
    }

    /**
     * 判断该淘宝商品是否属于ktv
     */
    private static boolean checkKtvProperty(TaobaoClient taobaoClient, OAuthToken token, String partnerTaobaoId) {
        ItemGetRequest req = new ItemGetRequest();
        req.setFields("cid");
        req.setNumIid(Long.parseLong(partnerTaobaoId));
        try {
            ItemGetResponse response = taobaoClient.execute(req, token.accessToken);
            if (StringUtils.isBlank(response.getErrorCode()) && response.getItem().getCid().equals(50019081L)) {
                return true;
            }
        } catch (ApiException e) {
            Logger.info(e, "get taobao props ,not ktv");
            return false;
        }
        return false;
    }
}
