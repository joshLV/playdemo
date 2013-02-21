package controllers;

import models.consumer.User;
import models.sms.SMSUtil;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.cache.Cache;
import play.mvc.Controller;

import com.uhuila.common.constants.DataConstants;
import com.uhuila.common.util.RandomNumberUtil;

/**
 * 找回密码.
 * User: yanjy
 */
public class FindPassword extends Controller {
    /**
     * 找回密码页面
     */
    public static void index() {
        render();
    }

    /**
     * 通过邮箱h或手机找回密码页面
     */
    public static void findByEmailOrMobile(String from) {
        String file_url = "";
        if (StringUtils.isNotEmpty(from) && "email".equals(from)) {
            file_url = "FindPassword/findByEmail.html";
        } else {
            file_url = "FindPassword/findByMobile.html";
        }
        renderTemplate(file_url);
    }


    /**
     * 通过邮箱或手机找回密码
     */
    public static void sendMessageCode(String from) {
        boolean isExisted = false;

        if (StringUtils.isNotEmpty(from) && from.indexOf("@") != -1) {
            isExisted = User.checkAndSendEmail(from);
        } else {

            isExisted = User.checkMobile(from);
            //手机存在
            if (isExisted) {

                String validCode = RandomNumberUtil.generateSerialNumber(4);
                String comment = "您的验证码是" + validCode + ", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
                SMSUtil.send(comment, from, "0000");
                //保存手机和验证码
                Cache.set("validCode_", validCode, "10mn");
                Cache.set("mobile_", from, "30mn");

            }
        }
        renderJSON(isExisted ? DataConstants.ONE.getValue() : DataConstants.ZERO.getValue());
    }

    /**
     * 判断手机和验证码是否正确
     *
     * @param mobile    手机
     * @param validCode 验证码
     */
    public static void checkMobile(String mobile, String validCode) {
        Object objCode = Cache.get("validCode_");
        Object objMobile = Cache.get("mobile_");
        String cacheValidCode = objCode == null ? "" : objCode.toString();
        if (Play.runingInTestMode()) {
            cacheValidCode = "123456";
        }
        String cacheMobile = objMobile == null ? "" : objMobile.toString();
        boolean isExisted = User.checkMobile(mobile);
        //手机不存在
        if (!isExisted) {
            renderJSON(DataConstants.THREE.getValue());
        }
        //判断验证码是否正确

        if (!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {

            renderJSON(DataConstants.ONE.getValue());
        }
        //判断手机是否正确


        if (!StringUtils.normalizeSpace(cacheMobile).equals(mobile)) {

            renderJSON(DataConstants.TWO.getValue());
        }
        Cache.delete("validCode_");

        renderJSON(DataConstants.ZERO.getValue());
    }

    /**
     * 找回密码页面
     */
    public static void resetPassword() {
        Object mobile = Cache.get("mobile_");
        String token = request.params.get("token");
        //判断发送邮件的链接是否有效
        boolean isExpired = User.isExpired(token);
        render(mobile, token, isExpired);
    }

    /**
     * 更新密码
     *
     * @param mobile 手机
     */
    public static void updatePassword(String token, String mobile, String password, String confirmPassword) {
        if (StringUtils.isBlank(token) && StringUtils.isBlank(mobile)) {
            renderJSON("-1");
        }

        //根据手机有邮箱更改密码
        User.updateFindPwd(token, mobile, password);

        Cache.delete("mobile_");
        Cache.delete("user_email_");
        renderJSON("1");
    }

    /**
     * 成功找回密码页面
     */
    public static void sendEmailSuccess() {
        render("FindPassword/sendEmailSuccess.html");
    }
}
