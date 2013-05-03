package controllers;

import models.mail.MailMessage;
import models.mail.MailUtil;
import models.operator.Operator;
import models.resale.Resaler;
import models.resale.ResalerCreditable;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import models.sms.SMSUtil;
import org.apache.commons.codec.digest.DigestUtils;
import play.Play;
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

    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("register_notification.resaler.email.receiver", "tangliqun@uhuila.com").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("register_notification.resaler.mobile", "").trim().split(",");

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

        //这里注册的分销商都是默认运营商的
        resaler.operator = Operator.defaultOperator();
        resaler.save();

        sendNotification(resaler);
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

    private static void sendNotification(Resaler resaler) {
        // 发邮件
        MailMessage message = new MailMessage();
        message.addRecipient(NOTIFICATION_EMAILS);
        message.setFrom("yibaiquan <noreplay@uhuila.com>");
        message.setSubject("分销商注册申请");
        message.putParam("resaler", resaler.loginName);
        message.setTemplate("resalerRegister");
        MailUtil.sendCommonMail(message);

        if(NOTIFICATION_MOBILES.length > 0 && !"".equals(NOTIFICATION_MOBILES[0])){
            SMSUtil.send("分销商注册申请，账号：" +resaler.loginName, NOTIFICATION_MOBILES);
        }
    }
}
