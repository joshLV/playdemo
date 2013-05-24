package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taobao.api.internal.util.WebUtils;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.mvc.Controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *
 */
public class KtvAuth extends Controller {
    public static void taobao(String code, String error, String error_description) {

        if (StringUtils.isBlank(code)) {
            render("KtvAuth/welcome.html");
        }


        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "authorization_code");
        param.put("code", code);
        param.put("client_id", "21519243");
        param.put("client_secret", "cb95f0bb25cbd99917696314cdb6bc43");
        param.put("redirect_uri", "http://api.quanfx.com/api/v1/ktv/taobao/welcome");
        param.put("scope", "item");
        param.put("view", "web");
//        param.put("state", state);
        String jsonResponse;
        try {
            jsonResponse = WebUtils.doPost("https://oauth.taobao.com/token", param, 3000, 3000);
        } catch (IOException e) {
            renderText(e);
            return;
        }


        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(jsonResponse).getAsJsonObject();

        String taobaoUserId = result.get("taobao_user_id").getAsString().trim();
        OAuthToken token = OAuthToken.find("byServiceUserIdAndWebSite", taobaoUserId, WebSite.TAOBAO).first();
        if (token == null) {
            token = new OAuthToken();
            token.webSite = WebSite.TAOBAO;
            token.accountType = AccountType.RESALER;
        }
        token.accessToken =  result.get("access_token").getAsString().trim();

        Date now = new Date();
        token.accessTokenExpiresAt = DateUtils.addMilliseconds(now,result.get("expires_in").getAsInt());

        token.refreshToken = result.get("refresh_token").getAsString().trim();
        token.refreshTokenExpiresAt = DateUtils.addMilliseconds(now,result.get("r1_expires_in").getAsInt());
        token.save();

        if (result.has(error)) {
            renderText("授权错误: " + result.get("error").getAsString() + " : ");
            return;
        }

        render("KtvAuth/success.html", result);
    }
}
