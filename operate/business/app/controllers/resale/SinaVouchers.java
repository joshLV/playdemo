package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OuterOrderPartner;
import models.sales.*;
import models.sina.SinaVoucherResponse;
import models.sina.SinaVoucherUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * User: yan
 * Date: 13-3-21
 * Time: 上午10:26
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class SinaVouchers extends Controller {
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(long goodsId) {
        Goods goods = Goods.findById(goodsId);

        Collection<Shop> shops = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, supplier, shops);
    }

    private static Map<String, Object> prepareRequestParams(Map<String, String> allParams) {
        //准备卡券描述
        Map<String, String> description = new HashMap<>();
        description.put("description", allParams.remove("description"));
        description.put("readme", allParams.remove("readme"));
        allParams.remove("body");
        allParams.remove("goodsId");
        allParams.remove("productId");

        //准备请求参数
        Map<String, Object> requestParams = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            requestParams.put(entry.getKey(), entry.getValue());
        }
        List<Map<String, String>> descriptions = new ArrayList<>();
        descriptions.add(description);

        requestParams.put("descriptions", descriptions);
        return requestParams;
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long goodsId) {
        Map<String, String> allParams = params.allSimple();
        Map<String, Object> requestParams = prepareRequestParams(allParams);

        SinaVoucherResponse response = SinaVoucherUtil.uploadTemplate(new Gson().toJson(requestParams));

        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            Goods goods = Goods.findById(goodsId);
            ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.SINA, goods);
            String contentId = response.content.getAsJsonObject().get("id").getAsString();
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id);
            product.partnerProduct(contentId).save();
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(requestParams),
                    ResalerProductJournalType.CREATE, "上传商品");
        }
        render("resale/SinaVouchers/result.html", response);
    }

    @ActiveNavigation("resale_partner_product")
    public static void edit(Long productId) {
        Map<String, String> allParams = params.allSimple();
        Map<String, Object> requestParams = prepareRequestParams(allParams);

        String log = new Gson().toJson(requestParams);//删除type前先保存一下
        requestParams.remove("type");

        SinaVoucherResponse response = SinaVoucherUtil.updateTemplate(new Gson().toJson(requestParams));

        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            ResalerProduct product = ResalerProduct.findById(productId);
            ResalerProductJournal.createJournal(product, operateUser.id, log,
                    ResalerProductJournalType.UPDATE, "修改商品");
        }
        render("resale/SinaVouchers/result.html", response);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showEdit(Long productId) {
        ResalerProduct resalerProduct = ResalerProduct.findById(productId);
        Goods goods = resalerProduct.goods;
        Collection<Shop> shops = goods.getShopList();

        ResalerProductJournal journal = ResalerProductJournal.find("product = ? order by createdAt desc", resalerProduct).first();

        render(goods, shops, resalerProduct, journal);
    }

    /**
     * 销卡
     */
    @ActiveNavigation("resale_partner_product")
    public static void dispose(String voucherId) {
        ECoupon coupon = ECoupon.find("partnerCouponId=? and partner=? and status =?", voucherId, OuterOrderPartner.SINA, ECouponStatus.UNCONSUMED).first();
        if (coupon == null) {
            error("not found sina voucher:%s" + voucherId);
            return;
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("id", voucherId);

        SinaVoucherResponse response = SinaVoucherUtil.updateTemplate(new Gson().toJson(requestParams));

        if (response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            ResalerProduct product = ResalerProduct.findById(coupon.orderItems.outerGoodsNo);
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(requestParams),
                    ResalerProductJournalType.UPDATE, "sina销卡");
        }
        render("resale/SinaVouchers/result.html", response);
    }


    @ActiveNavigation("resale_partner_product")
    public static void voucherStyles() {
        render();
    }
}
