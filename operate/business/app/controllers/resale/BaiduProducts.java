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
import java.text.SimpleDateFormat;
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

        String allCategoriesJson = BaiduUtil.firstCategoryJsonCache();
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
        Map<String, Object> requestMap = new HashMap<>();
        for (Map.Entry<String, String> entry : groupbuyInfoParams.entrySet()) {
            requestMap.put(entry.getKey(), entry.getValue());
        }

        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.BAIDU_LOGIN_NAME);
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.BD, resaler, goods);
        requestMap.put("brand", goods.brand.name);
        requestMap.put("tpid", String.valueOf(product.goodsLinkId));
        requestMap.put("stock_model", 1);
        requestMap.put("token_mode", 0);
        requestMap.put("province_id", 5);
        requestMap.put("city_id", 289);//上海
        requestMap.put("pack", "asdas");//上海
        requestMap.put("notice", "asdas");//上海
        requestMap.put("ext", "asdas");//上海
        String beginTime = groupbuyInfoParams.get("begin_time");
        String endTime = groupbuyInfoParams.get("end_time");
        String validTime = groupbuyInfoParams.get("valid_time");
        Long time = getUinxTime(beginTime);
        requestMap.put("begin_time", time / 1000L);
        time = getUinxTime(endTime);
        requestMap.put("end_time", time / 1000L);
        time = getUinxTime(validTime);
        requestMap.put("valid_time", time / 1000L);

        Map<String, String> sp = new HashMap<>();
        sp.put("service_no", "243534534");

        requestMap.put("spinfo", sp);
        //商家信息参数
        List<Map<String, Object>> partnerParams = new ArrayList<>();
        //构建商家信息参数
        for (String id : shopIds) {
            Map<String, Object> partnerParam = new HashMap<>();
            Map<String, Object> locationMap = new HashMap<>();
            for (String key : partnerKeys) {
                if (key.equals("lat")) {
                    locationMap.put("lat", requestMap.get(key + "_" + id));
                } else if (key.equals("lng")) {
                    locationMap.put("lng", requestMap.get(key + "_" + id));
                }
                partnerParam.put(key, requestMap.remove(key + "_" + id));
                partnerParam.remove("lng");
                partnerParam.remove("lat");
            }

            partnerParam.put("location", locationMap);
            partnerParams.add(partnerParam);
        }

        requestMap.remove("shopIds");
        requestMap.put("poi_array", partnerParams);
        //发起请求
        BaiduResponse response = BaiduUtil.sendRequest(requestMap, "createproduct.action");
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
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return sd.parse(beginTime).getTime();
        } catch (ParseException e) {
        }
        return 0L;
    }

}
