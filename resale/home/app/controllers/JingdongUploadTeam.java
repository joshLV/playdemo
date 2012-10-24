package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.resale.cas.SecureCAS;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.IdNameResponse;
import models.jingdong.groupbuy.response.UploadTeamResponse;
import models.resale.Resaler;
import models.sales.Shop;
import models.supplier.Supplier;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.*;

/**
 * @author likang
 *         Date: 12-10-16
 */
@With(SecureCAS.class)
public class JingdongUploadTeam extends Controller{
    public static final String CACHE_KEY = "JINGDGONG_API";

    public static void prepare(Long goodsId){
        Resaler resaler = SecureCAS.getResaler();
        if(!Resaler.JD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        Supplier supplier = Supplier.findById(goods.supplierId);
        //查询城市
        List<IdNameResponse> cities = cacheCities();
        //只使用默认城市:上海
        IdNameResponse city = null;
        for(IdNameResponse c : cities){
            if(c.name.equals("上海")){
                city = c;
            }
        }
        List<String> areaNames = new ArrayList<>();
        for(Shop shop : shops) {
            areaNames.add(shop.getAreaName());
        }

        //查询区域
        if(city != null){
            List<IdNameResponse> districts = cacheDistricts(city.id);
            renderArgs.put("districts", districts);
            //查询商圈
            Map<Long, List<IdNameResponse>> areas = cacheAreas(city.id);
            renderArgs.put("ares", areas);

            List<String> unknownAreas = new ArrayList<>();
            unknownAreas.addAll(areaNames);
            for(List<IdNameResponse> areaList : areas.values()){
                if(areaList == null){
                    continue;
                }
                for(IdNameResponse area: areaList){
                    if(areaNames.contains(area.name)){
                        unknownAreas.remove(area.name);
                    }
                }
            }
            renderArgs.put("unknownAreas", unknownAreas);
        }

        //查询一级分类
        List<IdNameResponse> categories = cacheCategories(0L);

        render(goods, supplier, city, categories, areaNames, shops);
    }

    private static List<IdNameResponse> cacheCategories(final Long categoryId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CATEGORIES_" + categoryId),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return JDGroupBuyUtil.queryCategory(categoryId);
                    }
                }
        );
    }

    private static Map<Long, List<IdNameResponse>> cacheAreas(Long cityId) {
        final List<IdNameResponse> districts = cacheDistricts(cityId);
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITY_" + cityId + "_AREAS"),
                new CacheCallBack<Map<Long, List<IdNameResponse>>>() {
                    @Override
                    public Map<Long, List<IdNameResponse>> loadData() {
                        Map<Long, List<IdNameResponse>> areaMap = new HashMap<>();
                        for (IdNameResponse district : districts) {
                            List<IdNameResponse> areas = JDGroupBuyUtil.queryArea(district.id);
                            areaMap.put(district.id, areas);
                        }
                        return areaMap;
                    }
                }
        );
    }

    private static List<IdNameResponse> cacheDistricts(final Long cityId) {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITY_" + cityId + "_DISTRICTS"),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return JDGroupBuyUtil.queryDistrict(cityId);
                    }
                }
        );
    }

    private static List<IdNameResponse> cacheCities() {
        return CacheHelper.getCache(
                CacheHelper.getCacheKey(CACHE_KEY, "CITIES"),
                new CacheCallBack<List<IdNameResponse>>() {
                    @Override
                    public List<IdNameResponse> loadData() {
                        return JDGroupBuyUtil.queryCity();
                    }
                });
    }

    public static void upload(Long venderTeamId, List<String> areas){
        Resaler resaler = SecureCAS.getResaler();
        if(!Resaler.JD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }
        Map<String, List<String>> areaMap = new HashMap<>();
        //构建商圈数据结构
        for(String area: areas) {
            String tmp[] = area.split("-");
            if(tmp.length != 2){
                continue;
            }
            String districtId = tmp[0];
            String areaId = tmp[1];
            if(areaMap.get(districtId) == null){
                List<String> areaIds = new ArrayList<>();
                areaIds.add(areaId);
                areaMap.put(districtId, areaIds);
            }else {
                areaMap.get(districtId).add(areaId);
            }
        }

        models.sales.Goods goods = models.sales.Goods.findById(venderTeamId);
        if(goods == null){
            error("goods not found: " + venderTeamId);return;
        }
        List<Shop> shops = Shop.find("bySupplierId", goods.supplierId).fetch();

        String url = JDGroupBuyUtil.GATEWAY_URL + "/platform/normal/uploadTeam.action";
        Template template = TemplateLoader.load("jingdong/groupbuy/request/uploadTeam.xml");
        Map<String, Object> params = new HashMap<>();
        for(Map.Entry<String, String> param : request.params.allSimple().entrySet()){
            params.put(param.getKey(), param.getValue());
        }
        params.put("shops", shops);
        params.put("areaMap", areaMap);
        String data = template.render(params);
        Logger.info("request, %s", data);
        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<UploadTeamResponse> uploadTeamRest = new JDRest<>();
        if(!uploadTeamRest.parse(response.getString(), new UploadTeamResponse())){
            render("JingdongUploadTeam/result.html", uploadTeamRest);
        }
        render("JingdongUploadTeam/result.html", uploadTeamRest);
    }

    public static void showTest(){
        render();
    }

    public static void test(String data){
        renderXml(JDGroupBuyUtil.decryptMessage(data));
    }
}
