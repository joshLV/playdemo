package models.sms.impl;

import models.sms.SMSException;
import models.sms.SMSMessage;
import models.sms.SMSProvider;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import util.ws.WebServiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 虚拟短信网络接口.
 * 用于内部测试使用的通道，不会发短信，而是保存到数据库表vx_sms中.
 *
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class VxSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
                    .getProperty("vxsms.http.send_url", "http://test1.quanfx.com/vxsms");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^0\\|");

    @Override
    public void send(SMSMessage message) {
        // 准备url
        String phoneArgs = StringUtils.join(message.getPhoneNumbers(), ",");

        Map<String, Object> qparams = new HashMap<>();
        qparams.put("xh", message.getCode());
        qparams.put("content", message.getContent());
        qparams.put("mobiles", phoneArgs);

        String result = WebServiceRequest.url(SEND_URL).type("VXSMS")
                .params(qparams)
                .addKeyword(phoneArgs).postString();

        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            // 发送失败
            throw new SMSException("发送虚拟短信不成功:" + result);
        }

    }

    @Override
    public String getProviderName() {
        return "VxSMSProvider";
    }

}
