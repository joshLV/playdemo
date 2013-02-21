package models.yihaodian;

import cache.CacheCallBack;
import cache.CacheHelper;
import org.w3c.dom.Node;
import play.libs.XPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-26
 */
public class YHDCategoryAPI {
    public static final String CACHE_KEY = "YIHAODIAN_API_CATEGORY_V2";

    /**
     * 根据商家id及父类别id查询商家被授权产品类别列表，只返回下一级。
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.products.get.html
     */
    public static List<Node> productCategories(Long parentId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryParentId", String.valueOf(parentId));

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.category.products.get", "categoryInfoList");
        if (response.isOk()) {
            return response.selectNodes("./categoryInfo");
        }
        return new ArrayList<>();
    }

    public static List<Node> productCategoriesCache(final Long parentId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "PRODUCT_CATEGORY_" + parentId ),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return productCategories(parentId);
                    }
                }
        );
    }

    /**
     * 根据商家id和父类别id 查询商家产品类别列表（店铺类别），只返回下一级。
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.merchant.products.get.html
     */
    public static List<Node> merchantCategories(Long parentId){
        Map<String, String> params = new HashMap<>();
        params.put("categoryParentId", String.valueOf(parentId));

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.category.merchant.products.get", "merchantCategoryInfoList");
        if (response.isOk()) {
            return response.selectNodes("./merchantCategoryInfo");
        }
        return new ArrayList<>();
    }

    public static List<Node> merchantCategoriesCache(final Long parentId){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "MERCHANT_CATEGORY_" + parentId),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return merchantCategories(parentId);
                    }
                }
        );
    }

    /**
     * 根据商家id查询商家被授权品牌列表
     * http://openapi.yihaodian.com/forward/inshop/yhd.category.brands.get.html
     */
    public static List<Node> brands(){
        Map<String, String> params = new HashMap<>();

        YHDResponse response = YHDUtil.sendRequest(params, "yhd.category.brands.get", "brandInfoList");
        if (response.isOk()) {
            return response.selectNodes("./brandInfo");
        }
        return new ArrayList<>();
    }

    public static List<Node> brandsCache(){
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "BRANDS"),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return brands();
                    }
                }
        );
    }
}
