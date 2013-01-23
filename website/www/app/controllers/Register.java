package controllers;

import com.uhuila.common.constants.DataConstants;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserWebIdentification;
import models.order.PromoteRebate;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;

/**
 * 前台注册用户
 *
 * @author yanjy
 */
@With(WebsiteInjector.class)
public class Register extends Controller {

    public static final String PROMOTER_COOKIE = "promoter_track";

    /**
     * 注册页面
     */
    public static void index(Boolean embed) {
        if (embed != null && embed == true) {
            render("/Register/embedIndex.html");
        }
        render();
    }

    /**
     * 注册新用户
     *
     * @param user 用户信息
     */
    public static void create(@Valid User user) {
        System.out.println("user>>>" + user);
        if (Validation.hasError("user.mobile")
                && Validation.hasError("user")) {
            Validation.clear();


        }
        System.out.println("user.loginName==" + user.loginName);
        if (User.checkLoginName(user.loginName)) {
            Validation.addError("user.loginName", "validation.loginName");
        }
        if (!user.password.equals(user.confirmPassword)) {
            Validation.addError("user.confirmPassword", "validation.confirmPassword");
        }

        if (!"dev".equals(play.Play.configuration.get("application.mode"))) {

            if (StringUtils.isNotEmpty(user.captcha) && !user.captcha.toUpperCase().equals(Cache.get(params.get("randomID")))) {
                Validation.addError("user.captcha", "validation.captcha");

            }
        }

        if (Validation.hasErrors()) {
            render("Register/index.html", user);
        }

        //用户创建
        user.create();
        user.userInfo = new UserInfo(user);
        //取得cookie中的推荐码
        Http.Cookie tj_cookie = request.cookies.get(PROMOTER_COOKIE);

        if (tj_cookie != null) {
            //记录推荐人和被推荐人的关系
            User promoterUser = User.getUserByPromoterCode(tj_cookie.value);
            if (promoterUser != null) {
                new PromoteRebate(promoterUser, user, null, BigDecimal.ZERO, true).save();
                user.promoteUserId = promoterUser.id;
            }
        }
        user.save();

        // 确保创建Account，以避免在消费时因并发而产生2个以上的Account
        AccountUtil.getConsumerAccount(user.id);

        if (WebsiteInjector.getUserWebIdentification() != null) {
            JPA.em().flush();
            UserWebIdentification uwi = UserWebIdentification.findOne(WebsiteInjector.getUserWebIdentification().cookieId);
            if (uwi == null) {
                uwi = WebsiteInjector.getUserWebIdentification();
                uwi.save();
            }
            if (uwi.registerCount == null) {
                uwi.registerCount = 0;
            }
            uwi.registerCount += 1;
            uwi.save();
        }

        renderArgs.put("count", 0);
        render("Register/registerSuccess.html", user);
    }


    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(String loginName, String mobile) {
        boolean isExisted = User.checkLoginName(loginName);
        renderJSON(isExisted ? DataConstants.ONE.getValue() : DataConstants.ZERO.getValue());
    }

    /**
     * 验证码
     *
     * @param randomID 随机数
     */
    public static void captcha(String randomID) {
        Images.Captcha captcha = Images.captcha();

        String code = captcha.getText("#49B07F", 4);
        Cache.set(randomID, code.toUpperCase(), "30mn");
        renderBinary(captcha);
    }

    /**
     * UUID
     *
     * @return
     */
    public static String genRandomId() {
        return Codec.UUID();
    }

}
