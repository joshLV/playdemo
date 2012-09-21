package controllers;

import play.cache.Cache;
import play.mvc.Controller;

/**
 * @author likang
 *         Date: 12-9-20
 */
public class CaptchaProvider extends Controller{

    public static void captcha(String id) {
        play.libs.Images.Captcha captcha = play.libs.Images.captcha();
        String code = captcha.getText("#96B729");
        Cache.set(id, code, "5mn");
        renderBinary(captcha);
    }
}
