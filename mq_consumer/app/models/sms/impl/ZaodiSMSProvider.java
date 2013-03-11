package models.sms.impl;

import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: tanglq
 * Date: 13-3-7
 * Time: 下午5:03
 */
public class ZaodiSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
            .getProperty("zaodisms.http.send_url");
    private final String USERNAME = Play.configuration
            .getProperty("zaodisms.http.username");
    private final String PASSWORD = Play.configuration
            .getProperty("zaodisms.http.password");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^\\d+,(\\d+)");

    @Override
    public void send(SMSMessage message) {
        List<String> avaiablePhoneNumbers = new ArrayList<>();
        for (String phone : message.getPhoneNumbers()) {
            if (phone != null && phone.length() >= 11) { // 手机号必须大于等于11位
                avaiablePhoneNumbers.add(phone);
            }
        }

        if (avaiablePhoneNumbers.size() == 0) {
            Logger.info("手机号位数不足：" + message);
            return;
        }

        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");
/*
序号	参数	说明
1	account	用户账号
2	pswd	用户密码
3	mobile	合法的手机号码，号码间用英文逗号分隔
4	msg	短信内容，短信内容长度不能超过585个字符。使用URL方式编码为UTF-8格式。短信内容超过70个字符（企信通是60个字符）时，会被拆分成多条，然后以长短信的格式发送。
5	needstatus	是否需要状态报告，取值true或false，true，表明需要状态报告；false不需要状态报告
 */
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("account", USERNAME));
        qparams.add(new BasicNameValuePair("pswd", PASSWORD));
        qparams.add(new BasicNameValuePair("mobile", phoneArgs));
        qparams.add(new BasicNameValuePair("msg", message.getContent()));
        String url = SEND_URL.replace(":sms_info",
                URLEncodedUtils.format(qparams, "UTF-8"));

        String result = WebServiceRequest.url(url).type("ZaodiSMS").addKeyword(phoneArgs).getString();
/*
20110725160412,0
1234567890100

代码	说明
0	提交成功
101	无此用户
102	密码错
103	提交过快（提交速度超过流速限制）
104	系统忙（因平台侧原因，暂时无法处理提交的短信）
105	敏感短信（短信内容包含敏感词）
106	消息长度错（>585或<=0）
107	包含错误的手机号码
108	手机号码个数错（群发>50000或<=0;单发>200或<=0）
109	无发送额度（该用户可用短信数已使用完）
110	不在发送时间内
 */
        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (m.find()) {
            String code = m.group(1);
            Logger.info("Matched " + RESULTCODE_PATTERN + ", code=" + code);
            if ("0".equals(code)) {
                // 发送失败
                Logger.info("发送兆帝短信成功：" + message.getPhoneNumbers());
                return;
            }
        } else {
            Logger.info("Not match.....");
        }
        throw new SMSException("发送兆帝短信不成功:" + result);
    }

}
