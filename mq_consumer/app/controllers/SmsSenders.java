package controllers;

import models.sms.SMSFactory;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;

/**
 * User: tanglq
 * Date: 13-3-7
 * Time: 下午9:45
 */
public class SmsSenders extends Controller {

    public static void send(String sp, String mobile, String content) {
        if (StringUtils.isBlank(sp) || StringUtils.isBlank(mobile) || StringUtils.isBlank(content)) {
            renderText("请指定smsProvider或mobile或content! sp=" + sp + ", mobile=" + mobile + ", content=" + content);
        }
        //content = "需要发中文消息内容"

        // curl -L "http://localhost:9401/sms/send?sp=zaodisms&content=%E3%80%9058%E5%9B%A2%E3%80%91%E5%B8%82%E5%9C%BA%E4%BB%B71118%E5%85%83%E7%9A%84%E7%9C%BC%E9%95%9C%E5%85%AC%E5%9C%BA%E9%85%8D%E9%95%9C%E5%A5%97%E7%BB%84%E7%94%B158%E5%90%88%E4%BD%9C%E5%95%86%E5%AE%B6%E3%80%90%E4%B8%80%E7%99%BE%E5%88%B8%E3%80%91%E6%8F%90%E4%BE%9B%2C%E4%B8%80%E7%99%BE%E5%88%B8%E5%8F%B7%E5%88%B8%E5%8F%B75909592505%2C%E6%9C%89%E6%95%88%E6%9C%9F%E8%87%B32013-06-30%E5%AE%A2%E6%9C%8D4007895858%E3%80%90%E4%B8%80%E7%99%BE%E5%88%B8%E3%80%91&mobile=15026682165"

        SMSProvider smsProvider = SMSFactory.getSMSProvider(sp);

        smsProvider.send(new SMSMessage(content, mobile));

        renderText(smsProvider.getClass().getName() + " send success!");
    }
}
