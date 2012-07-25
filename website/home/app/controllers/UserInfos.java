package controllers;

import com.uhuila.common.util.RandomNumberUtil;
import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sms.SMSUtil;
import org.apache.commons.lang.StringUtils;

import play.Play;
import play.cache.Cache;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserInfos extends Controller {

    /**
     * 用户资料页面
     */
    public static void index() {
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的资料", "/userInfo");
        User user = SecureCAS.getUser();
        UserInfo userInfo = UserInfo.findByUser(user);
        render(user, userInfo, breadcrumbs);
    }

    /**
     * 用户资料页面
     */
    public static void update(Long id, UserInfo userInfo, String interest) {
        User user = SecureCAS.getUser();
        UserInfo userInfos = UserInfo.find("user=?",user).first();
        System.out.print("<<<<");
        if (userInfos != null) {
            //存在则修改
        	System.out.print(">>>>");
        	System.out.print(userInfo.fullName);
        	System.out.print(".....");
        	
            userInfos.update(userInfo, interest);
            
            System.out.print("prev");
            System.out.print(userInfos.fullName);
            System.out.print(userInfos.interest);
            System.out.print("latter");
        }
        System.out.print("change!!");
   
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
        if (Play.runingInTestMode()){
        	validCode="123456";
        }
        String comment = "【券市场】您的验证码是" + validCode + ", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
        SMSUtil.send(comment, mobile, "0000");
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
        
        //System.out.print(cacheValidCode+"\n");
        
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

        Cache.delete("validCode_");
        Cache.delete("mobile_");
        renderJSON("0");
        
        
        
        
    }

}
