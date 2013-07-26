package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.baidu.BaiduResponse;
import models.baidu.BaiduUtil;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
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

        String allCategoriesJson = BaiduUtil.allCategoryJsonCache();
        Collection<Shop> shopList = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);
        String allCityJson = BaiduUtil.allCityJsonCache();
        render(goods, allCategoriesJson, allCityJson, shopList, supplier);
    }

    /**
     * 上报团购
     */
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
        String firstCatalog = groupbuyInfoParams.get("first_catalog");
        if (StringUtils.isBlank(firstCatalog) || !firstCatalog.contains(",")) {
            error("二级分类参数不合法！");
        }
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.BAIDU_LOGIN_NAME);
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.BD, resaler, goods);
        requestMap.put("brand", goods.brand.name);
        requestMap.put("tpid", String.valueOf(product.goodsLinkId));
        System.out.println(firstCatalog.substring(0, firstCatalog.indexOf(",")));
        requestMap.put("first_catalog", firstCatalog.substring(0, firstCatalog.indexOf(",")));
        requestMap.put("token_mode", 0);
        requestMap.put("province_id", 5);
        requestMap.put("city_id", 289);//上海

        //修改和添加的共同参数
        extractRequestParam(shopIds, groupbuyInfoParams, requestMap);

        //发起请求
        BaiduResponse response = BaiduUtil.sendRequest(requestMap, "createproduct.action");
        //保存历史
        if (response.isOk()) {
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            String partnerProductId = response.data.getAsJsonObject().get("groupon_id").getAsString();
            product.partnerProduct(partnerProductId).save();
            product.url("http://tuan.baidu.com/selftg/item/detail?city_id=3&item_id=" + partnerProductId);
            product.save();

            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(groupbuyInfoParams),
                    ResalerProductJournalType.CREATE, "上传商品");
        }

        render("resale/BaiduProducts/result.html", response);

    }

    /**
     * 转时间戳
     */
    private static Long getUinxTime(String date) {
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return sd.parse(date).getTime() / 1000L;
        } catch (ParseException e) {
        }
        return 0L;
    }

    /**
     * 下架团购商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void offsale(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("tpid", product.goodsLinkId);
        BaiduResponse response = BaiduUtil.sendRequest(requestMap, "restoreproduct.action");
        if (!response.isOk()) {
            render("resale/BaiduProducts/result.html", response);
        }
        product.status = ResalerProductStatus.OFFSALE;
        product.save();
        redirect("/resaler-products/products/bd/" + product.goods.id);
    }

    @ActiveNavigation("resale_partner_product")
    public static void showEdit(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }

        Supplier supplier = Supplier.findById(product.goods.supplierId);
        Collection<Shop> shopList = product.goods.getShopList();
        Goods goods = product.goods;
        String allCategoriesJson = BaiduUtil.allCategoryJsonCache();
        render(product, goods, allCategoriesJson, shopList, supplier);
    }


    /**
     * 编辑团购
     */
    @ActiveNavigation("resale_partner_product")
    public static void edit(Long productId, @As(",") List<String> shopIds) {
        OperateUser operateUser = OperateRbac.currentUser();
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }
        Map<String, String> groupbuyInfoParams = params.allSimple();

        groupbuyInfoParams.remove("body");
        groupbuyInfoParams.remove("goodsId");
        Map<String, Object> requestMap = new HashMap<>();
        for (Map.Entry<String, String> entry : groupbuyInfoParams.entrySet()) {
            requestMap.put(entry.getKey(), entry.getValue());
        }

//        extractRequestParam(shopIds, groupbuyInfoParams, requestMap);


        Map<String, Object> rMap = new HashMap<>();
        rMap.put("tpid", product.goodsLinkId);
        rMap.put("update", new Gson().toJson(requestMap));
        //发起请求
        BaiduResponse response = BaiduUtil.sendRequest(rMap, "updateproduct.action");
        //保存历史
        if (response.isOk()) {
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            String partnerProductId = response.data.getAsJsonObject().get("groupon_id").getAsString();
            product.partnerProduct(partnerProductId).save();
            product.url("http://tuan.baidu.com/selftg/item/detail?city_id=3&item_id=" + partnerProductId);
            product.save();

            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(groupbuyInfoParams),
                    ResalerProductJournalType.UPDATE, "修改商品");
        }

        render("resale/BaiduProducts/result.html", response);

    }

    private static void extractRequestParam(List<String> shopIds, Map<String, String> groupbuyInfoParams, Map<String, Object> requestMap) {
        requestMap.put("stock_model", 1);
        requestMap.put("begin_time", getUinxTime(groupbuyInfoParams.get("begin_time")));
        requestMap.put("end_time", getUinxTime(groupbuyInfoParams.get("end_time")));
        requestMap.put("valid_time", getUinxTime(groupbuyInfoParams.get("valid_time")));

        Map<String, String> sp = new HashMap<>();
        sp.put("service_no", "400-686-5151");
        sp.put("provider", "一百券");

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
    }
}
