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

    public static void prepare(Long goodsId){
        Resaler resaler = SecureCAS.getResaler();
        if(!Resaler.JD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        Supplier supplier = Supplier.findById(goods.supplierId);
        //查询城市
        List<IdNameResponse> cities = JDGroupBuyUtil.cacheCities();
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
            List<IdNameResponse> districts = JDGroupBuyUtil.cacheDistricts(city.id);
            renderArgs.put("districts", districts);
            //查询商圈
            Map<Long, List<IdNameResponse>> areas = JDGroupBuyUtil.cacheAreas(city.id);
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
        List<IdNameResponse> categories = JDGroupBuyUtil.cacheCategories(0L);
        Map<Long, List<IdNameResponse>> subCategories = new HashMap<>();
        for(IdNameResponse category : categories) {
            System.out.println("=======>" + category.id);
            List<IdNameResponse> subCategory = JDGroupBuyUtil.cacheCategories(category.id);
            subCategories.put(category.id, subCategory);
        }

        render(goods, supplier, city, categories, subCategories, areaNames, shops);
    }


    public static void upload(Long venderTeamId, List<String> areas, List<String> subGroupIds){
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
        Long groupId = null;
        List<Long> group2List = new ArrayList<>();
        for(String subGroupId : subGroupIds){
            String[] ids = subGroupId.split("-");
            if(ids.length == 1){
                groupId = Long.parseLong(ids[0]);
                break;
            }else if(ids.length == 2){
                groupId = Long.parseLong(ids[0]);
                group2List.add(Long.parseLong(ids[1]));
            }
        }

        models.sales.Goods goods = models.sales.Goods.findById(venderTeamId);
        if(goods == null){
            error("goods not found: " + venderTeamId);return;
        }
        Collection<Shop> shops = goods.getShopList();

        String url = JDGroupBuyUtil.GATEWAY_URL + "/platform/normal/uploadTeam.action";
        Template template = TemplateLoader.load("jingdong/groupbuy/request/uploadTeam.xml");
        Map<String, Object> params = new HashMap<>();
        for(Map.Entry<String, String> param : request.params.allSimple().entrySet()){
            params.put(param.getKey(), param.getValue());
        }
        params.put("shops", shops);
        params.put("areaMap", areaMap);
        params.put("groupId", groupId);
        params.put("group2List", group2List);
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
