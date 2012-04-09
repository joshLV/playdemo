package controllers;

import models.oauth.OauthToken;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.response.ItemAddResponse;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class TopClient extends Controller{
    private static final String url = "http://gw.api.taobao.com/router/rest";
    private static final String appkey = "12576100";
    private static final String appsecret = "b1301dd48f91e483b42c914bbdf38d01";
    
    
    public static void add(Long favId){
        Resaler user = SecureCAS.getResaler();
        OauthToken token = OauthToken.find("byUserId", user.getId().toString()).first();
        ResalerFav resalerFav = ResalerFav.findById(favId);
        if(resalerFav == null){
            error("no fav found");
        }
        if(token == null){
            redirect("http://container.api.taobao.com/container?appkey=12576100&encode=utf-8");
        }

        Goods goods = resalerFav.goods;
        TaobaoClient taobaoClient = new DefaultTaobaoClient(url, appkey, appsecret);
        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(goods.baseSale > 999999 ? 999999 : goods.baseSale);
        addRequest.setPrice(goods.getResalePrice(user.level).toString());
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(goods.name);
        addRequest.setDesc(goods.name);
        addRequest.setLocationState("上海");
        addRequest.setLocationCity("上海");
        addRequest.setCid(50022008L);
 
        ItemAddResponse response;
        try {
            response = taobaoClient.execute(addRequest, token.accessToken); //执行API请求并打印结果
            System.out.println("body:"+response.getBody());
            
            if(response.getErrorCode() != null){
                renderArgs.put("errMsg", "请求淘宝服务出现异常：ErrorCode:" + response.getErrorCode() + 
                        "; ErrorMessage:" + response.getMsg() +
                        "; SubErrorCode:" + response.getSubCode() + 
                        "; SubErrorMessage:" + response.getSubMsg());
                render("ResalerFavs/topError.html");
            }else{
                Long iid = response.getItem().getNumIid();
                resalerFav.taobaoItemId = iid;
                resalerFav.save();
                redirect("/library");
            }
            
            System.out.println("msg:" + response.getMsg());
        } catch (ApiException e) {
            Logger.error(e.toString());
            renderArgs.put("errMsg", "请求淘宝服务出现异常，请稍后再试");
            render("ResalerFavs/topError.html");
            
        }  
    }
}
