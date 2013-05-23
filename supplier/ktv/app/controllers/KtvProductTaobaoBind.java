package controllers;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.response.ItemGetResponse;
import controllers.supplier.SupplierInjector;
import models.accounts.AccountType;
import models.admin.SupplierUser;
import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.*;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
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
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List<Shop> shops = Shop.findShopBySupplier(supplierId);
        List<KtvProduct> products = KtvProduct.findProductBySupplier(supplierId);
        render(shops, products);
    }

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
                }
            } catch (ApiException e) {
                Logger.info(e, "update sku to taobao failed");
            }

        } else {
            renderJSON("{\"error\":\"无效的淘宝URL\"}");
        }

    }

    /**
     * 绑定产品,创建goods信息以及resalerProduct
     */
    public static void bindProduct(Shop shop, KtvProduct product, String taobaoProductId) {
        KtvProductGoods ktvProductGoods = KtvProductGoods.findGoods(shop, product);
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Resaler resaler = Resaler.findById(supplierUser.supplier.defaultResalerId);
        if (ktvProductGoods == null) {
            Goods goods = new Goods();
            goods.shops = new HashSet<>();
            goods.shops.add(shop);
            goods.status = GoodsStatus.ONSALE;
            goods.name = shop.name + product.name;
            goods.title = shop.name + product.name;
            goods.product = product;
            goods.supplierId = supplierUser.supplier.id;
            goods.salePrice = BigDecimal.ONE;
            goods.originalPrice = BigDecimal.ONE;
            goods.save();

            ktvProductGoods = new KtvProductGoods();
            ktvProductGoods.goods = goods;
            ktvProductGoods.shop = shop;
            ktvProductGoods.product = product;
            ktvProductGoods.save();

            ResalerProduct resalerProduct = ResalerProduct.alloc(OuterOrderPartner.TB, resaler, goods);
            resalerProduct.partnerProductId = taobaoProductId;
            resalerProduct.save();
            //保存商品状态
            resalerProduct.creator(supplierUser.id).partnerProduct(taobaoProductId)
                    .status(ResalerProductStatus.UPLOADED).save();

            renderJSON("{\"info\":\"" + shop.name + product.name + "\",\"taobaoProductId\":\"" + taobaoProductId + "\"}");
        }
        renderJSON("{\"error\":\"该商品已经绑定过了\"}");
    }
}
