package models.weixin;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.Play;
import play.libs.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 13-3-19
 */
public class WeixinUtil {
    public static String TOKEN = Play.configuration.getProperty("weixin.token");
    public static String WEIXIN_USERNAME = Play.configuration.getProperty("weixin.userName");

    public static WeixinRequest parseMessage(Document document) {
        WeixinRequest message = new WeixinRequest();
        message.message = document;

        message.createTime = new Date(1000L * Integer.parseInt(message.selectTextTrim("CreateTime")));
        message.fromUserName = message.selectTextTrim("FromUserName");
        message.toUserName = message.selectTextTrim("toUserName");
        message.msgType = WeixinMessageType.valueOf(message.selectTextTrim("MsgType").toUpperCase());

        return message;
    }

    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String sha1Signed = makeSignature(timestamp, nonce);
        return sha1Signed.equalsIgnoreCase(signature);
    }

    public static String makeSignature(String timestamp, String nonce) {
        List<String> params = new ArrayList<>();
        params.add(TOKEN);
        params.add(timestamp);
        params.add(nonce);
        Collections.sort(params);
        String paramsStr = StringUtils.join(params, "");
        return Codec.hexSHA1(paramsStr);
    }

}
