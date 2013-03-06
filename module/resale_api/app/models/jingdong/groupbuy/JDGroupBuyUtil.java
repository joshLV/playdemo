package models.jingdong.groupbuy;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.XML;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-28
 */
public class JDGroupBuyUtil {
    public static final String VENDER_ID = Play.configuration.getProperty("jingdong.vender_id");
    public static final String VENDER_KEY = Play.configuration.getProperty("jingdong.vender_key");
    public static final String AES_KEY = Play.configuration.getProperty("jingdong.aes_key");

    public static final String CODE_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    public static final String CODE_CHARSET = "utf-8";

    public static String GATEWAY_URL = Play.configuration.getProperty("jingdong.gateway.url", "http://gw.tuan.jd.net");

    /**
     * 向京东发起请求.
     *
     * 请保证 action 为接口的URL中的标识
     * 同时在 jingdong/groupbuy/request 包下有同名的xml模板文件
     *
     * @param action        请求标识，请使用接口URL中的名称
     * @param params        模板参数
     * @param keywords      关键词
     * @return              解析后的京东响应消息
     */
    public static JingdongMessage sendRequest(String action, Map<String, Object> params, String ... keywords) {
        //准备 url 和 模板数据
        String url = GATEWAY_URL + "/platform/normal/" + action + ".action";
        String templatePath = "jingdong/groupbuy/request/" + action + ".xml";
        Template template = TemplateLoader.load(templatePath);
        String data = (params != null) ? template.render(params) : template.render();

        //生成rest请求内容
        Logger.info("jingdong request %s:\n%s", action, data);
        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        Logger.info("jingdong request %s encrypted:\n%s", action, restRequest);

        //发起请求
        WebServiceRequest request = WebServiceRequest.url(url).type("jingdong."+action).requestBody(restRequest);
        for (String keyword : keywords) {
            request = request.addKeyword(keyword);
        }
        String response = request.postString();
        Logger.info("jingdong response %s:\n%s", action, response);
        //解析响应
        return parseMessage(response);
    }

    public static JingdongMessage parseMessage(String document) {
        Document xmlDocument;
        try{
            xmlDocument = XML.getDocument(document);
        }catch (Exception e) {
            Logger.info("jingdong message failed: xml parse error.");
            return new JingdongMessage();
        }
        return parseMessage(xmlDocument);
    }

    /**
     * 解析京东的消息。包括我们请求京东的接口后京东的响应，以及京东主动通知我们的信息.
     *
     * @param document  xml形式的京东消息
     * @return          解析后的京东消息
     */
    public static JingdongMessage parseMessage(Document document) {
        JingdongMessage message = new JingdongMessage();
        if (document == null) {
            Logger.info("jingdong message failed: empty document.");
            return message;
        }
        message.version = XPath.selectText("/*/Version", document).trim();
        try{
            message.venderId = Long.parseLong(StringUtils.trimToNull(XPath.selectText("/*/VenderId", document)));
            message.zip = Boolean.parseBoolean(StringUtils.trimToNull(XPath.selectText("/*/Zip", document)));
            message.encrypt = Boolean.parseBoolean(StringUtils.trimToNull(XPath.selectText("/*/Encrypt", document)));
        }catch (Exception e) {
            return message;
        }

        // 只有作为京东的响应的时候， resultCode 和 resultMessage 才有用
        message.resultMessage = StringUtils.trimToNull(XPath.selectText("/*/ResultMessage", document));
        message.resultCode = StringUtils.trimToNull(XPath.selectText("/*/ResultCode", document));

        if(message.encrypt){
            String rawMessage = XPath.selectText("/*/Data", document);
            if (rawMessage != null) {
                //解析加密字符串
                String decryptedMessage = JDGroupBuyUtil.decryptMessage(rawMessage);
                Logger.info("jingdong response decrypted:\n%s", decryptedMessage);

                message.message = XPath.selectNode("/Message", XML.getDocument(decryptedMessage));
            }

        } else{
            message.message = XPath.selectNode("/*/Data/Message", document);
        }
        if (!message.isOk()) {
            Logger.info("jingdong message failed");
        }

        return message;
    }

    /**
     * 解密REST信息
     *
     * @param message 解密前的信息
     * @return 解密后的信息
     */
    public static String decryptMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message to be decrypted can not be null");
        }
        if (AES_KEY == null) {
            throw new RuntimeException("no jingdong AES_KEY found");
        }

        try {
            // Base64解码
            byte[] base64Decoded = Base64.decodeBase64(message.getBytes(CODE_CHARSET));
            // AES解码
            byte[] raw = AES_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] aesEncodedBytes = cipher.doFinal(base64Decoded);

            return new String(aesEncodedBytes, CODE_CHARSET);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * 加密REST信息
     *
     * @param message 加密前的信息
     * @return 加密后的信息
     */
    public static String encryptMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message to be encrypted can not be null");
        }
        if (AES_KEY == null) {
            throw new RuntimeException("no jingdong AES_KEY found");
        }

        try {
            // AES编码
            byte[] raw = AES_KEY.getBytes(CODE_CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(CODE_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] messageBytes = message.getBytes(CODE_CHARSET);
            byte[] aesEncodedBytes = cipher.doFinal(messageBytes);
            // Base64编码
            return new String(Base64.encodeBase64(aesEncodedBytes), CODE_CHARSET);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * 添加基本的请求参数，渲染完整REST请求内容
     *
     * @param data 核心请求内容
     * @return 完整的REST请求内容
     */
    public static String makeRequestRest(String data) {
        Template template = TemplateLoader.load("jingdong/groupbuy/request/main.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("version", "1.0");
        params.put("venderId", VENDER_ID);
        params.put("venderKey", VENDER_KEY);
        params.put("encrypt", "true");
        params.put("zip", false);
        params.put("data", encryptMessage(data));

        return template.render(params);
    }

}
