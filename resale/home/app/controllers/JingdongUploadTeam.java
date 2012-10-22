package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.CategoryResponse;
import models.jingdong.groupbuy.response.CityResponse;
import models.jingdong.groupbuy.response.UploadTeamResponse;
import models.resale.Resaler;
import models.supplier.Supplier;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-10-16
 */
@With(SecureCAS.class)
public class JingdongUploadTeam extends Controller{
    private static String JD_LOGIN_NAME = "jingdong";
    public static void prepare(Long goodsId){
        Resaler resaler = SecureCAS.getResaler();
        if(!JD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        Supplier supplier = Supplier.findById(goods.supplierId);
        List<CityResponse> cities = JDGroupBuyUtil.queryCity();
        List<CategoryResponse> categories = JDGroupBuyUtil.queryCategory(0L);

        render(goods, supplier, cities, categories);
    }

    public static void upload(){
        Resaler resaler = SecureCAS.getResaler();
        if(!JD_LOGIN_NAME.equals(resaler.loginName)){
            error("there is nothing you can do");
        }

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
