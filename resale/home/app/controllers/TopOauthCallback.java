package controllers;

import models.oauth.OauthToken;
import models.resale.Resaler;
import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;
import play.mvc.Controller;
import play.mvc.With;
import sun.misc.BASE64Decoder;

/**
 * 用户订单确认控制器.
 * 淘宝oauth回调
 * 
 */
@With({SecureCAS.class, ResaleCAS.class})
public class TopOauthCallback extends Controller{
    private static BASE64Decoder base64Decoder = new BASE64Decoder(); 
    public static void index(String top_session, String top_sign, String top_parameters){
        //加载用户账户信息
        Resaler user = ResaleCAS.getResaler();
        OauthToken oauthToken = new OauthToken();
        oauthToken.userId = user.getId().toString();
        oauthToken.accessToken = top_session;
        oauthToken.save();
        redirect("/library");
    }
}
