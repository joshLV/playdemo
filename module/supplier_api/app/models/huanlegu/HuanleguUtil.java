package models.huanlegu;

import models.jingdong.groupbuy.JDGroupBuyUtil;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.Logger;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import play.libs.XML;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-6-19
 */
public class HuanleguUtil {
    public static final String CODE_CHARSET = "utf-8";

    public static String SECRET_KEY = "12345678";
    public static String DISTRIBUTOR_ID = "456";
    public static String CLIENT_ID = "789";

    public static String GATEWAY_URL = "http://example.com/api/send/";

    public static String encrypt(String content) {
        try {
            byte[] raw = SECRET_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            String desEncrypted = Codec.byteToHexString(cipher.doFinal(content.getBytes(CODE_CHARSET)));
            return Codec.encodeBASE64(desEncrypted);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    public static String decrypt(String content) {
        try {
            String value = new String(Codec.decodeBASE64(content), CODE_CHARSET);
            byte[] raw = SECRET_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return new String(cipher.doFinal(Codec.hexStringToByte(value)));
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    public static String sign(String serial, String content) {
        return Codec.encodeBASE64(
                Codec.hexMD5(serial + CLIENT_ID + DISTRIBUTOR_ID + content.length())
                        .toLowerCase()
        );
    }

    public static HuanleguMessage sendRequest(String action, Map<String, Object> params) {
        //准备 url 和 模板数据
        String url = GATEWAY_URL + action + "/";
        String templatePath = "huanlegu/request/" + action + ".xml";
        Template template = TemplateLoader.load(templatePath);
        String data = (params != null) ? template.render(params) : template.render();

        //生成rest请求内容
        Logger.info("huanlegu request %s:\n%s", action, data);
        String restRequest = makeRequestRest(data);
        Logger.info("huanlegu request %s encrypted:\n%s", action, restRequest);

        //发起请求
        WebServiceRequest request = WebServiceRequest.url(url).type("huanlegu."+action).requestBody(restRequest);

        String response = request.postString();
        Logger.info("huanlegu response %s:\n%s", action, response);
        //解析响应
        return parseMessage(response);

    }

    public static HuanleguMessage parseMessage(String document) {
        Document xmlDocument;
        try{
            xmlDocument = XML.getDocument(document);
        }catch (Exception e) {
            Logger.info("huanlegu message failed: xml parse error.");
            return new HuanleguMessage();
        }
        return parseMessage(xmlDocument);
    }

    /**
     * 解析欢乐谷的消息。
     *
     * @param document  xml形式的欢乐谷消息
     * @return          解析后的欢乐谷消息
     */
    public static HuanleguMessage parseMessage(Document document) {
        HuanleguMessage message = new HuanleguMessage();
        if (document == null) {
            Logger.info("huanlegu message failed: empty document.");
            return message;
        }
        message.version = StringUtils.trimToNull(XPath.selectText("/Trade/Head/Version", document).trim());
        message.timeStamp = StringUtils.trimToNull(XPath.selectText("/Trade/Head/TimeStamp", document));
        message.statusCode = StringUtils.trimToNull(XPath.selectText("/Trade/Head/StatusCode", document));
        message.sequenceId = StringUtils.trimToNull(XPath.selectText("/Trade/Head/SequenceId", document));
        message.sign = StringUtils.trimToNull(XPath.selectText("/Trade/Head/Signed", document));

        if (message.isOk()){
            String rawMessage = StringUtils.trimToNull(XPath.selectText("/Trade/Body", document));
            if (rawMessage != null) {
                //解析加密字符串
                String decryptedMessage = decrypt(rawMessage);
                if (verify(message.sequenceId, decryptedMessage, message.sign)) {
                    Logger.info("huanlegu response decrypted:\n%s", decryptedMessage);
                    message.message = XPath.selectNode("/Body", XML.getDocument("<Body>" + decryptedMessage + "</Body>"));
                }else {
                    Logger.error("huanlegu response error: invalid sign: %s", message.sign);
                }
            }
        }else {
            message.errorMsg = StringUtils.trimToNull(XPath.selectText("/Trade/Body/Message", document));
            Logger.info("huanlegu message failed: %s", message.errorMsg);
        }

        return message;
    }

    public static boolean verify(String serial, String decryptedContent, String sign) {
        return sign(serial, decryptedContent).equalsIgnoreCase(sign);
    }

    /**
     * 添加基本的请求参数，渲染完整REST请求内容
     *
     * @param data 核心请求内容
     * @return 完整的REST请求内容
     */
    public static String makeRequestRest(String data) {
        Template template = TemplateLoader.load("huanlegu/request.xml");
        Map<String, Object> params = new HashMap<>();
        String serial = String.valueOf(System.currentTimeMillis());
        params.put("version", "1");
        params.put("sequenceId", serial);
        params.put("distributorId", DISTRIBUTOR_ID);
        params.put("clientId", CLIENT_ID);
        params.put("timeStamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.put("sign", sign(serial, data));
        params.put("body", encrypt(data));

        return template.render(params);
    }
}
