package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uhuila.common.util.DateUtil;
import controllers.modules.resale.cas.SecureCAS;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-16
 * Time: 上午10:57
 */
@With(SecureCAS.class)
public class DDPushGoods extends Controller {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 批量发布商品
     *
     * @param goodsIds
     */
    public static void batchAdd(@As(",") List<Long> goodsIds) {
        Logger.info("DDAPIPushGoods API begin!");
        Resaler user = SecureCAS.getResaler();
        String failGoods = "";
        boolean pushFlag = true;
        for (Long goodsId : goodsIds) {
            Goods goods = Goods.findOnSale(goodsId);
            ResalerFav resalerFav = ResalerFav.find("byGoodsAndResaler", goods, user).first();
            if (resalerFav == null) {
                error("no fav found");
            }

            Map<String, Object> goodsArgs = new HashMap<>();
            List<Category> categoryList = Category.findByParent(0);//获取顶层分类
            Long categoryId = 0L;
            if (categoryList.size() > 0) {
                if (goods.categories != null && goods.categories.size() > 0 && goods.categories.iterator() != null && goods.categories.iterator().hasNext()) {
                    Category category = goods.categories.iterator().next();
                    categoryId = category.id;

                    if ((goods.topCategoryId == null || goods.topCategoryId == 0) && category.parentCategory != null) {
                        goods.topCategoryId = category.parentCategory.id;
                    }
                }
                if (goods.topCategoryId == null) {
                    goods.topCategoryId = categoryList.get(0).id;
                }
            }
            goodsArgs.put("categoryId", categoryId);
            goodsArgs.put("goods", goods);

            GoodsDeployRelation goodsMapping = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);

            goodsArgs.put("goodsMappingId", goodsMapping.linkId);
            Template template = TemplateLoader.load("DDPushGoods/pushGoods.xml");

            String requestParams = template.render(goodsArgs);
            try {
                pushFlag = DDAPIUtil.pushGoods(goodsMapping.linkId, requestParams);
            } catch (DDAPIInvokeException e) {
                Logger.info("[DDAPIPushGoods API] invoke push goods fail! goodsId=" + goodsId);
            }
            if (pushFlag) failGoods += "商品ID=" + goodsId + ",";
            if (!pushFlag) {
                resalerFav.partner = OuterOrderPartner.DD;
                resalerFav.save();
                Logger.info("[DDAPIPushGoods API] invoke push goods success!");
            }
        }

        renderJSON("{\"error\":\"" + pushFlag + "\",\"info\":\"" + failGoods + "\"}");
    }
    @Before
    public static void checkUser() {
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.DD_LOGIN_NAME.equals(user.loginName)) {
            error("user is not dangdang resaler");
        }
    }
    /**
     * 推送商品页面
     *
     * @param goodsId
     */
    public static void prepare(Long goodsId) {
        Logger.info("DDAPIPushGoods API begin!");
        Resaler user = SecureCAS.getResaler();
        //查询是否已经推送过该商品，是则直接从GoodsThirdSupport读取，不是就从goods表查询
        Goods goods = Goods.findOnSale(goodsId);
        ResalerFav resalerFav = ResalerFav.find("byGoodsAndResaler", goods, user).first();
        if (resalerFav == null) {
            error("no goods found");
        }
        Supplier supplier = Supplier.findById(goods.supplierId);

        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.DD);
        if (support == null) {
            getGoodsItems(goods);
        } else {
            getGoodsSupportItems(support);
        }
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        render(shops, supplier);
    }

    /**
     * 开始推送
     */
    public static void push() {
        Map<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        Long goodsId = Long.valueOf(StringUtils.trimToEmpty(params.get("goodsId")));
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        Resaler user = SecureCAS.getResaler();
        Goods goods = Goods.findOnSale(goodsId);
        ResalerFav resalerFav = ResalerFav.find("byGoodsAndResaler", goods, user).first();
        if (resalerFav == null) {
            error("no fav found");
        }
        String goodsData = gson.toJson(params);

        //查询是否已经推送过该商品，没有则创建，有则更新
        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.DD);
        if (support == null) {
            GoodsThirdSupport.generate(goods, goodsData, OuterOrderPartner.DD).save();
        } else {
            support.goodsData = goodsData;
            support.save();
        }

        if (StringUtils.isNotEmpty(params.get("save"))) {
            renderArgs.put("info", "更改成功！");
            render("/DDPushGoods/result.html");
        }
        Map<String, Object> goodsArgs = new HashMap<>();
        GoodsDeployRelation goodsMapping = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);
        // 设置参数
        setGoodsParams(params, goods, goodsArgs, goodsMapping);
        Template template = TemplateLoader.load("DDPushGoods/pushGoods1.xml");
        String requestParams = template.render(goodsArgs);
        boolean pushFlag = true;
        try {
            pushFlag = DDAPIUtil.pushGoods(goodsMapping.linkId, requestParams);
        } catch (DDAPIInvokeException e) {
            Logger.info("[DDAPIPushGoods API] invoke push goods fail! goodsId=" + goodsId);
        }
        if (!pushFlag) {
            resalerFav.partner = OuterOrderPartner.DD;
            resalerFav.save();
            Logger.info("[DDAPIPushGoods API] invoke push goods success!");
        }
        render("/DDPushGoods/result.html", pushFlag);
    }

    /**
     * 从Goods读取数据
     */

    private static void getGoodsItems(Goods goods) {
        renderArgs.put("name", goods.name);
        renderArgs.put("title", goods.title);
        renderArgs.put("shortName", goods.shortName);
        renderArgs.put("imageOriginalPath", goods.getImageOriginalPath());
        renderArgs.put("salePrice", goods.getResalePrice());
        renderArgs.put("faceValue", goods.faceValue);
        Date nowDate = DateUtil.getBeginOfDay();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date afterMonthDate = DateUtil.lastDayOfMonth(cal.getTime());
        renderArgs.put("effectiveAt", nowDate);
        renderArgs.put("expireAt", afterMonthDate);

        renderArgs.put("effectStartDate", nowDate);
        renderArgs.put("effectEndDate", goods.expireAt);
        renderArgs.put("teamMaxNum", goods.getRealStocks());
        renderArgs.put("teamMinNum", "1");
        renderArgs.put("limitMaxNum", (goods.limitNumber == null || goods.limitNumber == 0) ? "9999" : goods.limitNumber);
        renderArgs.put("limitOnceMax", "99999");
        renderArgs.put("limitOnceMin", "1");
        renderArgs.put("buyTimes", "9999");
        renderArgs.put("exhibition", goods.getExhibition());
        renderArgs.put("prompt", goods.getPrompt());
        renderArgs.put("details", goods.getDetails());
        renderArgs.put("supplierDes", goods.getSupplierDes());
        renderArgs.put("shopList", goods.getShopList());
        renderArgs.put("goodsId", goods.id);
    }

    /**
     * 从GoodsThirdSupport读取数据
     *
     * @param support
     */
    private static void getGoodsSupportItems(GoodsThirdSupport support) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        Map<String, String> map = gson.fromJson(support.goodsData, type);
        renderArgs.put("name", map.get("teamSummary"));
        renderArgs.put("title", map.get("teamTitle"));
        renderArgs.put("shortName", map.get("teamShortName"));
        renderArgs.put("salePrice", map.get("salePrice"));
        renderArgs.put("faceValue", map.get("originalPrice"));
        renderArgs.put("effectiveAt", DateUtil.stringToDate(map.get("beginTime"), DATE_FORMAT));
        renderArgs.put("expireAt", DateUtil.stringToDate(map.get("endTime"), DATE_FORMAT));
        renderArgs.put("teamMaxNum", map.get("teamMaxNum"));
        renderArgs.put("teamMinNum", map.get("teamMinNum"));
        renderArgs.put("limitMaxNum", map.get("limitMaxNum"));
        renderArgs.put("limitOnceMax", map.get("limitOnceMax"));
        renderArgs.put("limitOnceMin", map.get("limitOnceMin"));
        renderArgs.put("buyTimes", map.get("buyTimes"));
        renderArgs.put("teamDetail", StringUtils.trimToEmpty(map.get("teamDetail")));
        renderArgs.put("effectStartDate", DateUtil.stringToDate(map.get("effectStartDate"), DATE_FORMAT));
        renderArgs.put("effectEndDate", DateUtil.stringToDate(map.get("effectEndDate"), DATE_FORMAT));
        renderArgs.put("deliveryType", map.get("deliveryType"));
        renderArgs.put("imageOriginalPath", map.get("srcImage"));
        renderArgs.put("refundType", map.get("refundType"));
        renderArgs.put("notice", map.get("notice"));
        renderArgs.put("goodsId", support.goods.id);
    }

    /**
     * 推送时设置参数信息
     *
     * @param params
     * @param goods
     * @param goodsArgs
     * @param goodsMapping
     */
    private static void setGoodsParams(Map<String, String> params, Goods goods, Map<String, Object> goodsArgs, GoodsDeployRelation goodsMapping) {
        goodsArgs.put("goodsMappingId", goodsMapping.linkId);
        goodsArgs.put("teamSummary", StringUtils.trimToEmpty(params.get("teamSummary")));
        goodsArgs.put("teamShortName", StringUtils.trimToEmpty(params.get("teamShortName")));
        goodsArgs.put("teamTitle", StringUtils.trimToEmpty(params.get("teamTitle")));
        goodsArgs.put("category", StringUtils.trimToEmpty(params.get("category")));
        goodsArgs.put("sub_category", StringUtils.trimToEmpty(params.get("sub_category")));
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        goodsArgs.put("shops", shops);
        goodsArgs.put("supplierId", params.get("supplierId"));
        goodsArgs.put("effectiveAt", params.get("beginTime"));
        goodsArgs.put("expireAt", params.get("endTime"));
        goodsArgs.put("originalPrice", params.get("originalPrice"));
        goodsArgs.put("salePrice", params.get("salePrice"));
        goodsArgs.put("teamMaxNum", params.get("teamMaxNum"));
        goodsArgs.put("teamMinNum", params.get("teamMinNum"));
        goodsArgs.put("sourceSaleNum", goods.getRealSaleCount());
        goodsArgs.put("limitMaxNum", params.get("limitMaxNum"));
        goodsArgs.put("limitOnceMin", params.get("limitOnceMin"));
        goodsArgs.put("limitOnceMax", params.get("limitOnceMax"));
        goodsArgs.put("buyTimes", params.get("buyTimes"));
        goodsArgs.put("refundType", params.get("refundType"));
        goodsArgs.put("effectStartDate", params.get("effectStartDate"));
        goodsArgs.put("effectEndDate", params.get("effectEndDate"));
        goodsArgs.put("deliveryType", params.get("deliveryType"));
        goodsArgs.put("srcImage", StringUtils.trimToEmpty(params.get("srcImage")));
        goodsArgs.put("teamDetail", StringUtils.trimToEmpty(params.get("teamDetail")));
        goodsArgs.put("smsHelp", StringUtils.trimToEmpty(params.get("teamTitle")));
        String title = "【" + (goods.brand == null ? "一百券" : goods.brand.name) + "】" + goods.name + "-优惠券,优惠券网,代金券" +
                (StringUtils.isEmpty(goods.keywords) ? "" : ("【" + goods.keywords + "】"));

        goodsArgs.put("seoTitle", title);
        goodsArgs.put("keywords", StringUtils.trimToEmpty(goods.keywords));
        goodsArgs.put("notice", StringUtils.trimToEmpty(params.get("notice")));
    }
}
