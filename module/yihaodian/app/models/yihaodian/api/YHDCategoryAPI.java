package models.yihaodian.api;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import models.yihaodian.response.YHDAttributeInfo;
import models.yihaodian.response.YHDIdName;
import models.yihaodian.response.YHDMerchantCategory;
import models.yihaodian.response.YHDProductCategory;
import play.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-26
 */
public class YHDCategoryAPI {
    public static final String CACHE_KEY = "YIHAODIAN_API_CATEGORY";

    /**
     * 根据商家id及父类别id查询商家被授权产品类别列表，只返回下一级。
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.products.get.html
     */
    public static List<YHDProductCategory> productCategories(Long parentId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryParentId", String.valueOf(parentId));
        Logger.info("yhd.category.products.get categoryParentId %s", parentId);

        String responseXml = YHDUtil.sendRequest(params, "yhd.category.products.get");
        Logger.info("yhd.category.products.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YHDProductCategory> res = new YHDResponse<>();
            res.parseXml(responseXml, "categoryInfoList", true, YHDProductCategory.parser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return new ArrayList<>();
    }

    public static List<YHDProductCategory> productCategoriesCache(final Long parentId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "PRODUCT_CATEGORY_" + parentId),
                new CacheCallBack<List<YHDProductCategory>>() {
                    @Override
                    public List<YHDProductCategory> loadData() {
                        return productCategories(parentId);
                    }
                }
        );
    }

    /**
     * 根据商家id和父类别id 查询商家产品类别列表（店铺类别），只返回下一级。
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.merchant.products.get.html
     */
    public static List<YHDMerchantCategory> merchantCategories(Long parentId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryParentId", String.valueOf(parentId));
        Logger.info("yhd.category.merchant.products.get categoryParentId %s", parentId);

        String responseXml = YHDUtil.sendRequest(params, "yhd.category.merchant.products.get");
        Logger.info("yhd.category.merchant.products.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YHDMerchantCategory> res = new YHDResponse<>();
            res.parseXml(responseXml, "merchantCategoryInfoList", true, YHDMerchantCategory.parser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return new ArrayList<>();
    }

    public static List<YHDMerchantCategory> merchantCategoriesCache(final Long parentId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "MERCHANT_CATEGORY_" + parentId),
                new CacheCallBack<List<YHDMerchantCategory>>() {
                    @Override
                    public List<YHDMerchantCategory> loadData() {
                        return merchantCategories(parentId);
                    }
                }
        );
    }

    /**
     * 根据商家id查询商家被授权品牌列表
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.brands.get.html
     */
    public static List<YHDIdName> brands(){
        Map<String, String> params = new HashMap<>();
        Logger.info("yhd.category.brands.get ");

        String responseXml = YHDUtil.sendRequest(params, "yhd.category.brands.get");
        Logger.info("yhd.category.brands.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YHDIdName> res = new YHDResponse<>();
            res.parseXml(responseXml, "brandInfoList", true, YHDIdName.brandParser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return new ArrayList<>();
    }

    public static List<YHDIdName> brandsCache(){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "BRANDS"),
                new CacheCallBack<List<YHDIdName>>() {
                    @Override
                    public List<YHDIdName> loadData() {
                        return brands();
                    }
                }
        );
    }

    /**
     * 根据类别编码和属性编码查询类别属性值。
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.attribute.get.html
     */
    public static List<YHDAttributeInfo> attributeCategories(Long categoryId, Long attributeId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", String.valueOf(categoryId));
        params.put("attributeId", String.valueOf(attributeId));
        Logger.info("yhd.category.attribute.get categoryId:%s attributeId:%s", categoryId, attributeId);

        String responseXml = YHDUtil.sendRequest(params, "yhd.category.attribute.get");
        Logger.info("yhd.category.attribute.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YHDAttributeInfo> res = new YHDResponse<>();
            res.parseXml(responseXml, "categoryAttributeInfoList", true, YHDAttributeInfo.attributeCategoryParser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return new ArrayList<>();
    }

    public static List<YHDAttributeInfo> attributeCategoriesCache(final Long categoryId, final Long attributeId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "ATTRIBUTE_CATEGORY_" + categoryId + "_" + attributeId),
                new CacheCallBack<List<YHDAttributeInfo>>() {
                    @Override
                    public List<YHDAttributeInfo> loadData() {
                        return attributeCategories(categoryId, attributeId);
                    }
                }
        );
    }

    /**
     * 根据指定的商品类目查询类目系列属性
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.serial.attribute.get.html
     */
    public static List<YHDAttributeInfo> attributeSerials(Long categoryId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", String.valueOf(categoryId));
        Logger.info("yhd.category.serial.attribute.get categoryId:%s", categoryId);

        String responseXml = YHDUtil.sendRequest(params, "yhd.category.serial.attribute.get");
        Logger.info("yhd.category.serial.attribute.get response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<YHDAttributeInfo> res = new YHDResponse<>();
            res.parseXml(responseXml, "serialAttributeInfoList", true, YHDAttributeInfo.attributeCategoryParser);
            if(res.getErrorCount() == 0){
                return res.getVs();
            }
        }
        return new ArrayList<>();
    }

    public static List<YHDAttributeInfo> attributeSerialsCache(final Long categoryId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "ATTRIBUTE_SERIALS_" + categoryId),
                new CacheCallBack<List<YHDAttributeInfo>>() {
                    @Override
                    public List<YHDAttributeInfo> loadData() {
                        return attributeSerials(categoryId);
                    }
                }
        );
    }
}
