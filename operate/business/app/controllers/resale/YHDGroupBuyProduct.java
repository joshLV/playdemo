package controllers.resale;

import controllers.OperateRbac;
import models.order.OuterOrderPartner;
import models.resale.ResalePartnerProduct;
import models.sales.Goods;
import models.yihaodian.api.YHDCategoryAPI;
import models.yihaodian.response.YHDIdName;
import models.yihaodian.response.YHDMerchantCategory;
import models.yihaodian.response.YHDProductCategory;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author likang
 *         Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class YHDGroupBuyProduct extends Controller{
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        List<YHDProductCategory> categories = YHDCategoryAPI.productCategoriesCache(0L, false);
        List<YHDMerchantCategory> merchantCategories = YHDCategoryAPI.merchantCategoriesCache(0L, false);
        List<YHDIdName> brands = YHDCategoryAPI.brandsCache();

        render(goods, categories, merchantCategories, brands);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showProducts(Long goodsId) {
        request.params.allSimple();
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            Logger.info("goods not found");
            error("商品不存在");
        }
        List<ResalePartnerProduct> products = ResalePartnerProduct.find("byGoodsAndPartner",goods, OuterOrderPartner.YHD).fetch();
        render(products);
        /**
         *
         Map<String, String> params = new HashMap<>();
         params.put("productType", String.valueOf(productType));
         params.put("categoryId", String.valueOf(categoryId));
         params.put("merchantCategoryId", StringUtils.join(merchantCategoryId, ","));
         params.put("productCname", productCname);
         params.put("brandId", String.valueOf(brandId));
         params.put("outerId", String.valueOf(outerId));
         params.put("productMarketPrice", productMarketPrice.toString());
         params.put("productSalePrice",productSalePrice.toString());
         params.put("weight", String.valueOf(weight));
         params.put("virtualStockNum", String.valueOf(virtualStockNum));
         params.put("productDescription", productDescription);
         params.put("electronicCerticate", electronicCerticate);

         String responseXml = YHDUtil.sendRequest(params, "yhd.product.add");
         Logger.info("yhd.product.add response %s", responseXml);
         if (responseXml != null) {
         YHDResponse<UpdateResult> res = new YHDResponse<>();
         res.parseXml(responseXml, "updateCount", false, UpdateResult.parser);
         if(res.getErrorCount() > 0){
         renderArgs.put("errors", res.getErrors());
         }
         }
         render("YHDProduct/result.html");
         *
         */
    }
}
