package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taobao.api.internal.util.WebUtils;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;

import java.io.IOException;
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
        param.put("client_id", "21499637");
        param.put("client_secret", "38d71745714bbc3efa4f0dab45049cf6");
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
        if (result.has(error)) {
            renderText("授权错误: " + result.get("error").getAsString() + " : ");
            return;
        }

        render("KtvAuth/success.html", result);
    }
}
