package models.jingdong.groupbuy;

import cache.CacheCallBack;
import cache.CacheHelper;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.OuterOrder;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import util.extension.ExtensionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-2-2
 */
public class JDGroupBuyHelper {
    public static final String CACHE_KEY = "JINGDGONG_API_HELPER";

    /**
     * 在京东上验证.
     * 如果验证失败，则查询券信息，如果是已验证的则返回成功.
     *
     * @param coupon 想要验证的一百券的券.
     * @return 验证结果.
     */
    public static ExtensionResult verifyOnJingdong(ECoupon coupon){
        if (coupon.partner != ECouponPartner.JD) {
            return ExtensionResult.INVALID_CALL;
        }

        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("jingdong verify failed: outerOrder not found, couponId: " + coupon.id);
            return ExtensionResult.code(100).message("没有找到对应京东订单号（couponId:%d)", coupon.id);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("outerOrder", outerOrder);
        params.put("coupon", coupon);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("verifyCode", params);
        if (!response.isOk() || Integer.parseInt(response.selectTextTrim("./VerifyResult")) != 200) {
            response = JDGroupBuyUtil.sendRequest("queryCode", params);
            int jdCouonStatus = Integer.parseInt(response.selectTextTrim("./CouponStatus"));
            if (response.isOk() && jdCouonStatus == 10) {
                return ExtensionResult.SUCCESS;
            }
            // -1:不存在 0:成功 10:已验证使用过 20:已过期 30:已退款 40:已经使用后的退款 50:优惠券未发放 [必填]-->
            switch(jdCouonStatus) {
                case -1:
                    return ExtensionResult.code(101).message("券不存在");
                case 20:
                    return ExtensionResult.code(120).message("已过期");
                case 30:
                    return ExtensionResult.code(130).message("已退款");
                case 40:
                    return ExtensionResult.code(140).message("已使用后的退款");
                case 50:
                    return ExtensionResult.code(150).message("优惠券未发放");
                default:
                    return ExtensionResult.code(jdCouonStatus).message("未知京东接口返回代码: %d", jdCouonStatus);
            }
        }
        return ExtensionResult.SUCCESS;
    }

    /**
     * 查询京东的券状态.
     *
     * @param coupon 想要查询的一百券的券
     * @return 京东券状态
     *          -100    身份验证失败
     *          -1      优惠券不存在
     *          10      已验证使用过
     *          20      优惠券已过期
     *          30      优惠券已退款，优惠券退款处理中
     *          40      优惠券使用后退款
     */
    public static int couponStatus(ECoupon coupon) {
        if (coupon.partner != ECouponPartner.JD) {
            return -1;
        }

        OuterOrder outerOrder = OuterOrder.find("byYbqOrder", coupon.order).first();
        if (outerOrder == null) {
            Logger.info("jingdong couponStatus failed: outerOrder not found, couponId: " + coupon.id);
            return -1;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("outerOrder", outerOrder);
        params.put("coupon", coupon);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryCode", params);
        if(!response.isOk()) {
            return -1;
        }
        return  Integer.parseInt(response.selectTextTrim("./CouponStatus"));
    }

    /**
     * 查询上传商品时可用的分类
     * @param categoryId 父类别ID， 0代表查询根目录
     */
    public static List<Node> queryCategory(Long categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryCategoryList", params);
        if (response.isOk()) {
            return response.selectNodes("./Categories/Category");
        }
        return new ArrayList<>();
    }

    public static List<Node> cacheCategories(final Long categoryId) {
        if (Play.mode.isDev()) {
            return queryCategory(categoryId);
        }
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
        if (response.isOk()) {
            return response.selectNodes("./Cities/City");
        }
        return new ArrayList<>();
    }

    public static List<Node> cacheCities() {
        if (Play.mode.isDev()) {
            return queryCity();
        }
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
        Map<String, Object> params = new HashMap<>();
        params.put("cityId", cityId);

        JingdongMessage response = JDGroupBuyUtil.sendRequest("queryDistrictList", params);
        if (response.isOk()) {
            return response.selectNodes("./Districts/District");
        }
        return new ArrayList<>();
    }

    public static List<Node> cacheDistricts(final Long cityId) {
        if (Play.mode.isDev()) {
            return queryDistrict(cityId);
        }
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
        if (response.isOk()) {
            return response.selectNodes("./Areas/Area");
        }
        return new ArrayList<>();
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
