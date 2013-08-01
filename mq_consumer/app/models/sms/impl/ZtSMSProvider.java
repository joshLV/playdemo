package models.sms.impl;

import models.order.OrderItemsFee;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 上海助通网络接口.
 *
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class ZtSMSProvider implements SMSProvider {

    private final String SEND_URL = Play.configuration
            .getProperty("ztsms.http.send_url");
    private final String USERNAME = Play.configuration
            .getProperty("ztsms.http.username");
    private final String PASSWORD = Play.configuration
            .getProperty("ztsms.http.password");
    private final String PRODUCT_ID = Play.configuration
            .getProperty("ztsms.product_id");

    private final Pattern RESULTCODE_PATTERN = Pattern.compile("^1,");

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

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("username", USERNAME));
        qparams.add(new BasicNameValuePair("password", PASSWORD));
        // FIXME: 助通要求xh只能2位
        String xh = message.getCode();
        if (StringUtils.isNotBlank(xh)) {
            xh = xh.substring(0, 2);
        }

        qparams.add(new BasicNameValuePair("xh", xh));
        qparams.add(new BasicNameValuePair("productid", PRODUCT_ID));

        qparams.add(new BasicNameValuePair("content", message.getContent()));
        qparams.add(new BasicNameValuePair("mobile", phoneArgs));
        String url = SEND_URL.replace(":sms_info",
                URLEncodedUtils.format(qparams, "UTF-8"));

        String result = WebServiceRequest.url(url).type("ZTSMS").addKeyword(phoneArgs).getString();

        Logger.info("返回消息：" + result);
        result = result.trim();
        Matcher m = RESULTCODE_PATTERN.matcher(result);
        if (!m.find()) {
            if (result.startsWith("13")) {
                Logger.info("忽略13：30分钟内重复发送.");
            } else if ("3".equals(result.trim())) {
                Logger.info("忽略3：黑词审核中");
            } else {
                // 发送失败
                throw new SMSException("发送助通短信不成功:" + result);
            }
        }
        Logger.info("ZtSMS message.getOrderItemsId()=" + message.getOrderItemsId());
        if (message.getOrderItemsId() != null) {
            int smsCount = (int) (result.length() / 67) + 1;
            Logger.info("       smsCount=" + smsCount);
            OrderItemsFee.recordFee(message.getOrderItemsId(), message.getFeeType(),
                    getSmsFee().multiply(BigDecimal.valueOf(smsCount)));
        }
    }

    private static final BigDecimal smsFee = new BigDecimal("0.055");

    public BigDecimal getSmsFee() {
        return smsFee;
    }

}
