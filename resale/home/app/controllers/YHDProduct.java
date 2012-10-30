package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import models.yihaodian.api.YHDCategoryAPI;
import models.yihaodian.response.YHDIdName;
import models.yihaodian.response.YHDMerchantCategory;
import models.yihaodian.response.YHDProductCategory;
import models.yihaodian.shop.UpdateResult;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-19
 */
@With(SecureCAS.class)
public class YHDProduct extends Controller{
    public static void prepare(long goodsId){
        Resaler resaler = SecureCAS.getResaler();
        if(!Resaler.YHD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        List<YHDProductCategory> categories = YHDCategoryAPI.productCategoriesCache(0L);
        List<YHDMerchantCategory> merchantCategories = YHDCategoryAPI.merchantCategoriesCache(0L);
        List<YHDIdName> brands = YHDCategoryAPI.brandsCache();

        render(goods, categories, merchantCategories, brands);
    }

    public static void upload(Integer productType, Long categoryId, Long[] merchantCategoryId, String productCname,
                              long brandId, long outerId, BigDecimal productMarketPrice, BigDecimal productSalePrice,
                              Long weight, Long virtualStockNum, String productDescription, String electronicCerticate){
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
        render();
    }
}
