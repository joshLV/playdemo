package models.jingdong.groupbuy;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.OuterOrder;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-2-2
 */
public class JDGroupBuyHelper {
    public static String GATEWAY_URL = Play.configuration.getProperty("jingdong.gateway.url", "http://gw.tuan.360buy.net");

    public static final String CACHE_KEY = "JINGDGONG_API_HELPER";

    /**
     * 在京东上验证.
     *
     * @param coupon 想要验证的一百券的券.
     * @return 验证结果.
     */
    public static boolean verifyOnJingdong(ECoupon coupon){
        if (coupon.partner != ECouponPartner.JD) {
            return false;
        }

        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("jingdong verify failed: outerOrder not found, couponId: " + coupon.id);
            return false;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("outerOrder", outerOrder);
        params.put("coupon", coupon);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("verifyCode", params);
        return response.isOk();
    }

    /**
     * 查询上传商品时可用的分类
     * @param categoryId 父类别ID， 0代表查询根目录
     */
    public static List<Node> queryCategory(Long categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryCategoryList", params);
        return response.selectNodes("./Categories/Category");
    }

    public static List<Node> cacheCategories(final Long categoryId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CATEGORIES_" + categoryId),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return queryCategory(categoryId);
                    }
                }
        );
    }

    /**
     * 查询上传商品时可选城市.
     */
    public static List<Node> queryCity() {
        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryCityList", null);
        return response.selectNodes("./Cities/City");
    }

    public static List<Node> cacheCities() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITIES"),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return JDGroupBuyHelper.queryCity();
                    }
                });
    }



    /**
     * 查询城市区域
     *
     * @param cityId 城市ID
     * @return 城市区域列表
     */
    public static List<Node> queryDistrict(Long cityId) {
        String url = GATEWAY_URL + "/platform/normal/queryDistrictList.action";
        String templatePath = "jingdong/groupbuy/request/queryDistrict.xml";
        Map<String, Object> params = new HashMap<>();
        params.put("cityId", cityId);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryDistrictList", params);
        return response.selectNodes("./Districts/District");
    }

    public static List<Node> cacheDistricts(final Long cityId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITY_" + cityId + "_DISTRICTS"),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return queryDistrict(cityId);
                    }
                }
        );
    }


    /**
     * 查询商圈
     *
     * @param districtId 区域ID
     * @return 商圈列表
     */
    public static List<Node> queryArea(Long districtId) {
        Map<String, Object> params = new HashMap<>();
        params.put("districtId", districtId);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryAreaList", params);
        return response.selectNodes("./Areas/Area");
    }

    public static List<Node> cacheAreas(final Long districtId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "DISTRICT_" + districtId + "_AREAS"),
                new CacheCallBack<List<Node>>() {
                    @Override
                    public List<Node> loadData() {
                        return queryArea(districtId);
                    }
                }
        );
    }
}
