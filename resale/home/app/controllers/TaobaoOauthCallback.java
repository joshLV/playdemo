package controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.taobao.api.internal.util.TaobaoUtils;

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
public class TaobaoOauthCallback extends Controller{
    public static void index(String top_session, String top_sign, String top_parameters){
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        OauthToken oauthToken = new OauthToken();
        oauthToken.userId = user.getId();
        oauthToken.accessToken = top_session;
        try {
            Map<String, String> topParams = TaobaoUtils.decodeTopParams(top_parameters);
            System.out.println("top_session:" + top_session );
            for(String key : topParams.keySet()){
                System.out.println("top_params: "+key + ":" + topParams.get(key));
            }
            
            int expiresIn = Integer.parseInt(topParams.get("expires_in"));
            int reExpiresIn = Integer.parseInt(topParams.get("re_expires_in"));
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.SECOND, expiresIn);
            oauthToken.accessTokenExpiresAt = calendar.getTime();
            calendar.add(Calendar.SECOND, reExpiresIn - expiresIn);
            oauthToken.refreshTokenExpiresAt = calendar.getTime();

            oauthToken.save();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            redirect("/library");
        }
        redirect("/library");
    }
}
