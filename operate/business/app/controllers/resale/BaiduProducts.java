package controllers.resale;

import com.google.gson.Gson;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RandomNumberUtil;
import controllers.ImportCoupons;
import controllers.OperateRbac;
import models.baidu.BaiduResponse;
import models.baidu.BaiduUtil;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.*;
import models.supplier.Supplier;
import models.wuba.WubaResponse;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.h2.util.DateTimeUtils;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.text.ParseException;
import java.util.*;

/**
 * User: yan
 * Date: 13-7-12
 * Time: 上午10:02
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class BaiduProducts extends Controller {
    private static String[] partnerKeys = new String[]{
            "province_name", "city_name", "area_name", "shop_range", "name", "telephone", "address",
            "map_type", "lat", "lng", "open_time", "traffic_info"};

    /**
     * 展示上传页面
     */
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        String allCategoriesJson = BaiduUtil.allProductTypesJsonCache();

        Collection<Shop> shopList = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);
        String allCityJson = BaiduUtil.allCityJsonCache();
        render(goods, allCategoriesJson, allCityJson, shopList, supplier);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(long goodsId, @As(",") List<String> shopIds) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }

        //先将所有的参数认为是是团购信息参数
        Map<String, String> groupbuyInfoParams = params.allSimple();
        groupbuyInfoParams.remove("body");
        groupbuyInfoParams.remove("goodsId");
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.BAIDU_LOGIN_NAME);
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.BD, resaler, goods);
        groupbuyInfoParams.put("brand", "一百券");
        groupbuyInfoParams.put("tpid", String.valueOf(product.goodsLinkId));
        groupbuyInfoParams.put("stock_model", "1");
        groupbuyInfoParams.put("token_mode", "1");
        groupbuyInfoParams.put("province_id", "5");
        String beginTime = groupbuyInfoParams.get("begin_time");
        String endTime = groupbuyInfoParams.get("end_time");
        String validTime = groupbuyInfoParams.get("valid_time");
        Long time = getUinxTime(beginTime);
        groupbuyInfoParams.put("begin_time", String.valueOf(time / 1000L));
        time = getUinxTime(endTime);
        groupbuyInfoParams.put("end_time", String.valueOf(time / 1000L));
        time = getUinxTime(validTime);
        groupbuyInfoParams.put("valid_time", String.valueOf(time / 1000L));
        int maxSale = Integer.parseInt(groupbuyInfoParams.get("max_sale"));
        List<String> couponList = new ArrayList();
        for (int i = 0; i < maxSale; i++) {
            String coupon = generateAvailableEcouponSn(11);
            couponList.add(coupon);
            new ImportedCouponTemp(goods, coupon);
        }
        groupbuyInfoParams.put("thirdparty_tokens", StringUtils.join(couponList, ","));

        //商家信息参数
        List<Map<String, String>> partnerParams = new ArrayList<>();
        //构建商家信息参数
        for (String id : shopIds) {
            Map<String, String> partnerParam = new HashMap<>();
            Map<String, String> locationMap = new HashMap();
            for (String key : partnerKeys) {
                if (key.equals("lat")) {
                    locationMap.put("lat", groupbuyInfoParams.get(key + "_" + id));
                } else if (key.equals("lng")) {
                    locationMap.put("lng", groupbuyInfoParams.get(key + "_" + id));
                }
                partnerParam.put(key, groupbuyInfoParams.remove(key + "_" + id));
                partnerParam.remove("lng");
                partnerParam.remove("lat");
            }

            partnerParam.put("location", new Gson().toJson(locationMap));
            partnerParams.add(partnerParam);
        }

        groupbuyInfoParams.remove("shopIds");
        groupbuyInfoParams.put("poi_array", "{" + new Gson().toJson(partnerParams) + "}");
        System.out.println(groupbuyInfoParams+"-------");
        //发起请求
        BaiduResponse response = BaiduUtil.sendRequest(groupbuyInfoParams, "createproduct.action");
        //保存历史
        if (response.isOk()) {
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            String partnerProductId = response.data.getAsJsonObject().get("groupon_id").getAsString();
            product.partnerProduct(partnerProductId).save();
//            product.url("http://t.58.com/"+ firstCity + "/"+ partnerProductId);
            product.save();

            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(groupbuyInfoParams),
                    ResalerProductJournalType.CREATE, "上传商品");
        }

        render("resale/BaiduProducts/result.html", response);

    }

    private static Long getUinxTime(String beginTime) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beginTime).getTime();
        } catch (ParseException e) {
        }
        return 0L;
    }

    /**
     * 生成消费者唯一的券号.
     */
    private static String generateAvailableEcouponSn(int length) {
        String randomNumber;
        do {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing.
            }
            randomNumber = RandomNumberUtil.generateSerialNumber(length);
        } while (isNotUniqueEcouponSn(randomNumber));
        return randomNumber;
    }

    private static boolean isNotUniqueEcouponSn(String randomNumber) {
        return ImportedCoupon.find("from ImportedCoupon where coupon=?", randomNumber).fetch().size() > 0;
    }
}
