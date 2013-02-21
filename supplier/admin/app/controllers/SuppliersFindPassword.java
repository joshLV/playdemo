package controllers;

import com.uhuila.common.constants.DataConstants;
import com.uhuila.common.util.RandomNumberUtil;
import models.admin.SupplierUser;
import models.sms.SMSUtil;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.mvc.Controller;

public class SuppliersFindPassword extends Controller {

    /**
     * 用户信息
     */
    public static void index() {
        render();
    }

    /**
     * 通过手机发送验证码
     *
     * @param mobile 手机
     */
    public static void checkByTel(String mobile) {
        boolean isExisted = SupplierUser.checkMobile(mobile);
        //手机存在
        if (isExisted) {
            String validCode = RandomNumberUtil.generateSerialNumber(4);
            String comment = "您的验证码是" + validCode + ", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
            SMSUtil.send(comment, mobile, "0000");
            //保存手机和验证码
            Cache.set("validCode_", validCode, "30mn");
            Cache.set("mobile_", mobile, "30mn");
        }
        renderJSON(isExisted ? "1" : "0");
    }

    /**
     * 判断手机和验证码是否正确
     *
     * @param mobile    手机
     * @param validCode 验证码
     */
    public static void reset(String mobile, String validCode) {
        Object objCode = Cache.get("validCode_");
        Object objMobile = Cache.get("mobile_");
        String cacheValidCode = objCode == null ? "" : objCode.toString();
        String cacheMobile = objMobile == null ? "" : objMobile.toString();
        boolean isExisted = SupplierUser.checkMobile(mobile);

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
        SupplierUser supplierUser = SupplierUser.find("mobile", mobile).first();
        Cache.set("setMobile", mobile, "30mn");
        render(mobile, supplierUser);
    }

    /**
     * 更新密码
     *
     * @param mobile 手机
     */
    public static void updatePassword(Long supplierUserId, String mobile, String password, String confirmPassword) {
        Object objMobile = Cache.get("mobile_");
        if (!mobile.equals(objMobile)) {
            renderJSON("-1");
        }
        if (StringUtils.isBlank(String.valueOf(supplierUserId)) && StringUtils.isBlank(mobile)) {
            renderJSON("-2");
        }

        //根据手机有邮箱更改密码
        SupplierUser.updateFindPwd(supplierUserId, mobile, password);

        Cache.delete("mobile_");
        renderJSON("1");
    }
}
