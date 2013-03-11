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
 * 威智短信通道.
 *
 * 网站：http://www.vzdx.com/ http://www.scchit.com
 * 后台：http://sms.vzdx.com/index.php
 * api: http://sms.vzdx.com/api
 *
 * 用户名： 15026682165 密码：
 */
public class WzSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
            .getProperty("wzsms.http.send_url");
    private final String API_KEY = Play.configuration
            .getProperty("wzsms.http.apikey");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("$\\d{1,3}");

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

        /*
参数名称 类型 说明
apikey string 【必填项】32位用户识别码，请在后台获取，注意保密。
message string 【必填项】短信内容，可发长短信，300字限制。GET方式不允许短信内容中间有空格。
mobiles string  【必填项】手机号码，以英文逗号“,”隔开。GET方式限制100个，POST方式限制3000个。
encode string 【选填项】0表示GBK格式编码，1表示UTF-8格式编码；默认为0（GBK格式编码）。
sendtime string 【选填项】发送时间，为空或者早于当前时间表示即刻发送，11位整数，使用此选项请注意转换成TIME整数形式。默认为即可发送。
onethenone string 【选填项】1表示按组提交，每组的号码个数由用户后台设置有关。其他表示一次性提交。默认为一次性提交。
         */
        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("apikey", API_KEY));
        qparams.add(new BasicNameValuePair("message", message.getContent()));
        qparams.add(new BasicNameValuePair("mobiles", phoneArgs));
        qparams.add(new BasicNameValuePair("encode", "1"));
        String url = SEND_URL.replace(":sms_info",
                URLEncodedUtils.format(qparams, "UTF-8"));
        Logger.info("wzsms.url=" + url);
/*
错误描述 任务编号 发送短信成功
2 非法APIKey
3 非法IP（后台可设置，不设置表示允许所有IP提交）
4 发送内容为空
5 手机号码为空
6 余额不足
7 预设发送时间不为空，且不符合时间格式。
8 账户被锁定
其他 系统错误
 */
        String result = WebServiceRequest.url(url).type("WZSMS").addKeyword(phoneArgs).getString();

        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (m.find()) {
            throw new SMSException("发送威智短信不成功:" + result);
        }
    }
}
