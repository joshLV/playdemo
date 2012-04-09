package controllers;

import models.oauth.OauthToken;
import models.resale.Resaler;
import play.mvc.Controller;
import play.mvc.With;
import sun.misc.BASE64Decoder;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 用户订单确认控制器.
 * 淘宝oauth回调
 * 
 */
@With(SecureCAS.class)
public class TopOauthCallback extends Controller{
    private static BASE64Decoder base64Decoder = new BASE64Decoder(); 
    public static void index(String top_session, String top_sign, String top_parameters){
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        OauthToken oauthToken = new OauthToken();
        oauthToken.userId = user.getId().toString();
        oauthToken.accessToken = top_session;
        oauthToken.save();
        redirect("/library");
    }
}
