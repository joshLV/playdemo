package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uhuila.common.util.DateUtil;
import controllers.modules.resale.cas.SecureCAS;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.IdNameResponse;
import models.jingdong.groupbuy.response.UploadTeamResponse;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-16
 */
@With(SecureCAS.class)
public class JingdongUploadTeam extends Controller {
    public static final String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT1 = "yyyy-MM-dd";

    public static void prepare(Long goodsId) {
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.JD_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }
        Goods goods = Goods.findById(goodsId);

        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.JD);
        if (support == null) {

            getGoodsItems(goods);
        } else {
            getGoodsSupportItems(support);
        }
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        Supplier supplier = Supplier.findById(goods.supplierId);
        //查询城市
        List<IdNameResponse> cities = JDGroupBuyUtil.cacheCities();
        //只使用默认城市:上海
        IdNameResponse city = null;
        for (IdNameResponse c : cities) {
            if (c.name.equals("上海")) {
                city = c;
            }
        }
        List<String> areaNames = new ArrayList<>();
        for (Shop shop : shops) {
            areaNames.add(shop.getAreaName());
        }

        //查询区域
        if (city != null) {
            List<IdNameResponse> districts = JDGroupBuyUtil.cacheDistricts(city.id);
            renderArgs.put("districts", districts);
            //查询商圈
            Map<Long, List<IdNameResponse>> areas = JDGroupBuyUtil.cacheAreas(city.id);
            renderArgs.put("ares", areas);

            List<String> unknownAreas = new ArrayList<>();
            unknownAreas.addAll(areaNames);
            for (List<IdNameResponse> areaList : areas.values()) {
                if (areaList == null) {
                    continue;
                }
                for (IdNameResponse area : areaList) {
                    if (areaNames.contains(area.name)) {
                        unknownAreas.remove(area.name);
                    }
                }
            }
            renderArgs.put("unknownAreas", unknownAreas);
        }

        //查询一级分类
        List<IdNameResponse> categories = JDGroupBuyUtil.cacheCategories(0L);
        Map<Long, List<IdNameResponse>> subCategories = new HashMap<>();
        for (IdNameResponse category : categories) {
            List<IdNameResponse> subCategory = JDGroupBuyUtil.cacheCategories(category.id);
            subCategories.put(category.id, subCategory);
        }

        render(supplier, city, categories, subCategories, areaNames, shops);
    }


    public static void upload(Long venderTeamId, List<String> areas, List<String> subGroupIds) {
        Map<String, String> allParams = request.params.allSimple();
        allParams.remove("body");
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String> param : allParams.entrySet()) {
            params.put(param.getKey(), param.getValue());
        }
        Resaler resaler = SecureCAS.getResaler();
        if (!Resaler.JD_LOGIN_NAME.equals(resaler.loginName)) {
            error("there is nothing you can do");
        }
        Map<String, List<String>> areaMap = new HashMap<>();
        //构建商圈数据结构
        for (String area : areas) {
            String tmp[] = area.split("-");
            if (tmp.length != 2) {
                continue;
            }
            String districtId = tmp[0];
            String areaId = tmp[1];
            if (areaMap.get(districtId) == null) {
                List<String> areaIds = new ArrayList<>();
                areaIds.add(areaId);
                areaMap.put(districtId, areaIds);
            } else {
                areaMap.get(districtId).add(areaId);
            }
        }
        Long groupId = null;
        List<Long> group2List = new ArrayList<>();
        for (String subGroupId : subGroupIds) {
            String[] ids = subGroupId.split("-");
            if (ids.length == 1) {
                groupId = Long.parseLong(ids[0]);
                break;
            } else if (ids.length == 2) {
                groupId = Long.parseLong(ids[0]);
                group2List.add(Long.parseLong(ids[1]));
            }
        }

        allParams.put("areas", StringUtils.join(areas.toArray(), ","));
        allParams.put("subGroupIds", StringUtils.join(subGroupIds.toArray(), ","));
        Goods goods = Goods.findById(venderTeamId);
        if (goods == null) {
            error("goods not found: " + venderTeamId);
            return;
        }
        String goodsData = new Gson().toJson(allParams);

        //查询是否已经推送过该商品，没有则创建，有则更新
        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.JD);
        if (support == null) {
            GoodsThirdSupport.generate(goods, goodsData, OuterOrderPartner.JD).save();
        } else {
            support.goodsData = goodsData;
            support.save();
        }

        Collection<Shop> shops = goods.getShopList();

        String url = JDGroupBuyUtil.GATEWAY_URL + "/platform/normal/uploadTeam.action";
        Template template = TemplateLoader.load("jingdong/groupbuy/request/uploadTeam.xml");


        params.put("shops", shops);
        params.put("areaMap", areaMap);
        params.put("groupId", groupId);
        params.put("group2List", group2List);
        String data = template.render(params);
        Logger.info("request, %s", data);
        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<UploadTeamResponse> uploadTeamRest = new JDRest<>();
        if (!uploadTeamRest.parse(response.getString(), new UploadTeamResponse())) {
            render("JingdongUploadTeam/result.html", uploadTeamRest);
        }
        render("JingdongUploadTeam/result.html", uploadTeamRest);
    }

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
        Date afterMonthDate = DateUtil.lastDayOfMonth(cal.getTime());
        renderArgs.put("effectiveAt", nowDate);
        renderArgs.put("expireAt", afterMonthDate);
        renderArgs.put("couponExpireTime", goods.expireAt);
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
        JsonElement jsonElement = new JsonParser().parse(support.goodsData);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        renderArgs.put("name", jsonObject.get("teamTitle").getAsString());
        renderArgs.put("title", jsonObject.get("summary").getAsString());
        renderArgs.put("shortName", jsonObject.get("title").getAsString());
        renderArgs.put("imageOriginalPath", jsonObject.get("grouponBImg").getAsString());
        renderArgs.put("salePrice", jsonObject.get("teamPrice").getAsBigDecimal());
        renderArgs.put("faceValue", jsonObject.get("marketPrice").getAsBigDecimal());
        renderArgs.put("effectiveAt", DateUtil.stringToDate(jsonObject.get("beginTime").getAsString(), DATE_FORMAT1));
        renderArgs.put("expireAt", DateUtil.stringToDate(jsonObject.get("endTime").getAsString(), DATE_FORMAT1));
        renderArgs.put("couponExpireTime", DateUtil.stringToDate(jsonObject.get("couponExpireTime").getAsString(), DATE_FORMAT1));
        renderArgs.put("notice", StringUtils.trimToEmpty(jsonObject.get("notice").getAsString()));
        renderArgs.put("teamDetail", StringUtils.trimToEmpty(jsonObject.get("teamDetail").getAsString()));
        renderArgs.put("goodsId", support.goods.id);
        renderArgs.put("areas", jsonObject.get("areas").getAsString());
        renderArgs.put("subGroupIds", jsonObject.get("subGroupIds").getAsString());
    }

    public static void showTest() {
        render();
    }

    public static void test(String data) {
        renderXml(JDGroupBuyUtil.decryptMessage(data));
    }
}
