package controllers;

import models.resale.Resaler;
import models.resale.ResalerCreditable;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.mvc.Controller;

import java.util.Date;

/**
 * 分销商注册
 *
 * @author yanjy
 */
public class ResalerRegister extends Controller {

    /**
     * 注册页面
     */
    public static void index() {
        render();
    }

    /**
     * 注册新用户
     *
     * @param resaler 用户信息
     */
    public static void create(@Valid Resaler resaler) {

        String returnFlag = Resaler.checkValue(resaler.loginName, resaler.mobile);
        //用户名存在
        if ("1".equals(returnFlag)) {
            Validation.addError("resaler.loginName", "validation.loginName");
        }
        //手机存在
        if ("2".equals(returnFlag)) {
            Validation.addError("resaler.mobile", "validation.mobile");
        }

        if (!resaler.password.equals(resaler.confirmPassword)) {
            Validation.addError("resaler.confirmPassword", "两次密码输入的不一样！！");
        }
        if (Validation.hasErrors()) {
            render("ResalerRegister/index.html", resaler);
        }

        Images.Captcha captcha = Images.captcha();
        String passwordSalt = captcha.getText(6);
        //密码加密
        resaler.password = DigestUtils.md5Hex(resaler.password + passwordSalt);
        //正常
        resaler.status = ResalerStatus.PENDING;
        //随机码
        resaler.passwordSalt = passwordSalt;
        resaler.level= ResalerLevel.NORMAL;
        resaler.createdAt = new Date();
        resaler.loginName = resaler.loginName.toLowerCase().trim();
        resaler.creditable = ResalerCreditable.NO;
        resaler.save();

        render("ResalerRegister/success.html");
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(String loginName, String mobile) {
        String returnFlag = Resaler.checkValue(loginName, mobile);
        renderJSON(returnFlag);
    }
}
