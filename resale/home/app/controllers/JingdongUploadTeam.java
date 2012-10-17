package controllers;

import models.jingdong.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.CategoryResponse;
import models.jingdong.groupbuy.response.CityResponse;
import models.jingdong.groupbuy.response.UploadTeamResponse;
import models.supplier.Supplier;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class JingdongUploadTeam extends Controller{
    public static void prepare(Long goodsId){
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        Supplier supplier = Supplier.findById(goods.supplierId);
        List<CityResponse> cities = JDGroupBuyUtil.queryCity();
        List<CategoryResponse> categories = JDGroupBuyUtil.queryCategory(0L);

        render(goods, supplier, cities, categories);
    }

    public static void upload(){
        String url = JDGroupBuyUtil.GATEWAY_URL + "/platform/normal/uploadTeam.action";
        Template template = TemplateLoader.load("jingdong/groupbuy/request/uploadTeam.xml");
        Map<String, Object> params = new HashMap<>();
        for(Map.Entry<String, String> param : request.params.allSimple().entrySet()){
            params.put(param.getKey(), param.getValue());
        }
        String data = template.render(params);
        Logger.info("request, %s", data);
        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<UploadTeamResponse> uploadTeamRest = new JDRest<>();
        if(!uploadTeamRest.parse(response.getString(), new UploadTeamResponse())){
            String error = response.getString();
            render("JingdongUploadTeam/result.html", error);
        }
        render("JingDongUploadTeam/result.html", uploadTeamRest);
    }

    public static void showTest(){
        render();
    }

    public static void test(String data){
        renderXml(JDGroupBuyUtil.decryptMessage(data));
    }
}
