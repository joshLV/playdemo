package controllers;

import com.uhuila.common.util.RandomNumberUtil;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sms.BindMobile;
import models.sms.MobileBindType;
import models.sms.SMSMessage;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserInfos extends Controller {
    public static String BIND_MOBILE_ADD_MONEY = Play.configuration.getProperty("bind_mobile.promotion.add_money", "off");
    public static String BIND_MOBILE_ADD_MONEY_AMOUNT = Play.configuration.getProperty("bind_mobile.promotion.add_money.amount", "0");

    /**
     * 用户资料页面
     */
    public static void index() {
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的资料", "/userInfo");
        User user = SecureCAS.getUser();
        UserInfo userInfo = UserInfo.findByUser(user);
        render(user, userInfo, breadcrumbs);
    }

    public static void edit() {
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的资料", "/userInfo");
        User user = SecureCAS.getUser();
        UserInfo userInfo = UserInfo.findByUser(user);
        render(user, userInfo, breadcrumbs);
    }

    /**
     * 用户资料页面
     */
    public static void update(UserInfo userInfo, String interest) {
        User user = SecureCAS.getUser();
        UserInfo userInfos = UserInfo.find("user=?", user).first();
        if (userInfos != null) {
            //存在则修改	
            userInfos.update(userInfo, interest);
        }
        index();
    }

    /**
     * 发送验证码
     *
     * @param mobile 手机
     */
    public static void sendValidCode(String mobile, String oldMobile) {
        //判断旧手机号码是否存在
        if (StringUtils.isNotBlank(oldMobile) && !User.checkMobile(oldMobile)) {
            renderJSON("3");
        }
        //判断新手机号码是否存在
        if (User.checkMobile(mobile)) {
            renderJSON("2");
        }
        String validCode = RandomNumberUtil.generateSerialNumber(4);
        if (Play.runingInTestMode()) {
            validCode = "123456";
        }
        String comment = "您的验证码是" + validCode + ", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
        new SMSMessage(comment, mobile, "0000").send();
        //保存手机和验证码
        Cache.set("validCode_", validCode, "10mn");
        Cache.set("mobile_", mobile, "10mn");
        renderJSON("1");
    }

    /**
     * 绑定手机
     *
     * @param mobile 手机
     */
    public static void bindMobile(String mobile, String oldMobile, String validCode) {
        //判断旧手机号码是否存在
        if (StringUtils.isNotBlank(oldMobile) && !User.checkMobile(oldMobile)) {
            renderJSON("3");
        }
        Object objCode = Cache.get("validCode_");
        Object objMobile = Cache.get("mobile_");
        String cacheValidCode = objCode == null ? "" : objCode.toString();
        String cacheMobile = objMobile == null ? "" : objMobile.toString();

        //判断验证码是否正确
        if (!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {
            renderJSON("1");
        }

        //判断手机是否正确
        if (!StringUtils.normalizeSpace(cacheMobile).equals(mobile)) {
            renderJSON("2");
        }
        //更新用户基本信息手机
        User user = SecureCAS.getUser();
        user.updateMobile(mobile);
        if (BindMobile.find("byMobileAndBindType", mobile, MobileBindType.BIND_CONSUME).first() == null &&
                BindMobile.find("byBindTypeAndBindInfo", MobileBindType.BIND_CONSUME, String.valueOf(user.getId())).first() == null) {
            BindMobile bindMobile = new BindMobile(mobile, MobileBindType.BIND_CONSUME);
            bindMobile.bindInfo = String.valueOf(user.getId());
            bindMobile.save();
            if (BIND_MOBILE_ADD_MONEY.equals("on")) {
                Account account = AccountUtil.getConsumerAccount(user.getId());
                BigDecimal promotionAmount = new BigDecimal(BIND_MOBILE_ADD_MONEY_AMOUNT);
                TradeBill tradeBill = TradeUtil.promotionChargeTrade(account, promotionAmount).make();
                TradeUtil.success(tradeBill, "绑定手机送" + promotionAmount + "元");
            }
        }

        Cache.delete("validCode_");
        Cache.delete("mobile_");
        renderJSON("0");
    }

}
