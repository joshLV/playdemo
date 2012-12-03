package controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.taobao.api.internal.util.TaobaoUtils;

import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.resale.Resaler;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 用户订单确认控制器.
 * 淘宝oauth回调
 * 
 */
@With(SecureCAS.class)
public class TaobaoOauthCallback extends Controller{
    private static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    private static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "12621657");
    private static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "b0d06603b45a281f783b6ccd72ad8745");

    public static void index(String top_session, String top_sign, String top_parameters){
        //加载用户账户信息
        Resaler user = SecureCAS.getResaler();
        OAuthToken oAuthToken = OAuthToken.getOAuthToken(user.getId(), AccountType.RESALER, WebSite.TAOBAO);
        if(oAuthToken == null){
            oAuthToken = new OAuthToken();
            oAuthToken.userId = user.getId();
            oAuthToken.accountType = AccountType.RESALER;
            oAuthToken.webSite = WebSite.TAOBAO;
        }

        oAuthToken.accessToken = top_session;
        try {
            Map<String, String> topParams = TaobaoUtils.decodeTopParams(top_parameters);

            // 保存淘宝账户ID
            String serviceUserId = topParams.get("visitor_id");
            oAuthToken.serviceUserId = serviceUserId;

            // 计算并保存失效时间
            int expiresIn = Integer.parseInt(topParams.get("expires_in"));
            int reExpiresIn = Integer.parseInt(topParams.get("re_expires_in"));
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.SECOND, expiresIn);
            oAuthToken.accessTokenExpiresAt = calendar.getTime();
            calendar.add(Calendar.SECOND, reExpiresIn - expiresIn);
            oAuthToken.refreshTokenExpiresAt = calendar.getTime();

            oAuthToken.save();

        } catch (IOException e) {
            Logger.error(e, "oauth callback failed");
            redirect("/library");
        }
        redirect("/library");
    }

}
