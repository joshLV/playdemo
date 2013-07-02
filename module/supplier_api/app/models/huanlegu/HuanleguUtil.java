package models.huanlegu;

import models.order.ECoupon;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.Codec;
import play.libs.XML;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceRequest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.math.BigDecimal;
import java.security.SecureRandom;
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

    public static String SECRET_KEY = Play.configuration.getProperty("huanlegu.secret_key", "D7A38EFD4E1D4F42B5C198CEF8727022");
    public static String DISTRIBUTOR_ID = Play.configuration.getProperty("huanlegu.distributor_id", "10001097");
    public static String CLIENT_ID = Play.configuration.getProperty("huanlegu.client_id", "HVC000000065");
    public static String GATEWAY_URL = Play.configuration.getProperty("huanlegu.gateway_url", "http://202.104.133.113:8060/api/send/");

    public static final String SUPPLIER_DOMAIN_NAME = "huanlegu";

    public static HuanleguMessage getSightInfo(String sightId, String sightName) {
        Map<String, Object> params = new HashMap<>();
        params.put("sightId", sightId);
        params.put("sightName", sightName);

        return sendRequest("getSightInfo", params);
    }


    public static HuanleguMessage resend(ECoupon coupon) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageIndex", 1);
        params.put("pageSize", 1);
        params.put("orderId", coupon.order.orderNumber);
        params.put("hvOrderId", coupon.order.supplierOrderNumber);
        params.put("voucherId", coupon.supplierECouponId);

        return sendRequest("resendVoucher", params);
    }

    public static HuanleguMessage confirmOrder(ECoupon coupon, int quantity) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", coupon.order.id);
        params.put("dealTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(coupon.order.createdAt));
        params.put("name", "");
        params.put("mobile", coupon.orderItems.phone);
        params.put("quantity", quantity);
        params.put("ticketId", coupon.goods.supplierGoodsNo);
        params.put("salePrice", coupon.salePrice.toString());
        params.put("isSendSms", 1);
        params.put("certificateType", "0");//不需要证件
        params.put("certificateNum", "");
        params.put("appointTripDate", coupon.appointmentDate == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(coupon.appointmentDate));

        return sendRequest("confirmOrder", params);
    }

    public static HuanleguMessage confirmOrder(String orderNumber, Date dealTime, String mobile, int quantity,
               String ticketId, BigDecimal salePrice, Date appointmentDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderNumber);
        params.put("dealTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dealTime));
        params.put("name", "");
        params.put("mobile", mobile);
        params.put("quantity", quantity);
        params.put("ticketId", ticketId);
        params.put("salePrice", salePrice.toString());
        params.put("isSendSms", 1);
        params.put("certificateType", "0");//不需要证件
        params.put("certificateNum", "");
        params.put("appointTripDate", appointmentDate == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(appointmentDate));

        return sendRequest("confirmOrder", params);
    }

    public static HuanleguMessage checkTicketBuy(String mobile, int quantity, String ticketId, BigDecimal retailPrice,
             Date appointTripDate ) {
        Map<String, Object> params = new HashMap<>();
        params.put("ticketId", ticketId);
        params.put("quantity", quantity);
        params.put("retailPrice", retailPrice);
        params.put("appointTripDate", appointTripDate);
        params.put("mobile", mobile);
        params.put("certificateNum", "");

        return sendRequest("checkTicketBuy", params);
    }

    public static String encrypt(String content) {
        try {
            SecureRandom sr = new SecureRandom();
            DESKeySpec dks = new DESKeySpec(SECRET_KEY.getBytes(CODE_CHARSET));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
            String desEncrypted = Codec.byteToHexString(cipher.doFinal(content.getBytes(CODE_CHARSET)));
            return Codec.encodeBASE64(desEncrypted);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    public static String decrypt(String content) {
        try{
            String base64Decoded = new String(Codec.decodeBASE64(content), CODE_CHARSET);
            byte[] hexDecoded = Codec.hexStringToByte(base64Decoded);
            SecureRandom sr = new SecureRandom();
            DESKeySpec dks = new DESKeySpec(SECRET_KEY.getBytes(CODE_CHARSET));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
            return new String(cipher.doFinal(hexDecoded));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    public static String sign(String serial, String content) {
        System.out.println(serial+DISTRIBUTOR_ID+CLIENT_ID+content.length());
        System.out.println(Codec.hexMD5(serial+DISTRIBUTOR_ID+CLIENT_ID+content.length()));
        System.out.println(Codec.encodeBASE64(Codec.hexMD5(serial+DISTRIBUTOR_ID+CLIENT_ID+content.length())));
        return Codec.encodeBASE64(
                Codec.hexMD5(serial + DISTRIBUTOR_ID + CLIENT_ID + content.length())
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

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("xmlContent", restRequest);
        //发起请求
        WebServiceRequest request = WebServiceRequest.url(url).type("huanlegu."+action).params(requestParams);

        String response = request.postString();
        Logger.info("huanlegu response %s:\n%s", action, response);
        //解析响应
        return parseMessage(response, true);

    }

    public static HuanleguMessage parseMessage(String document, boolean isResponse) {
        Document xmlDocument;
        try{
            xmlDocument = XML.getDocument(document);
        }catch (Exception e) {
            Logger.info("huanlegu message failed: xml parse error.");
            return new HuanleguMessage();
        }
        return parseMessage(xmlDocument, isResponse);
    }


    /**
     * 解析欢乐谷的消息。
     *
     * @param document  xml形式的欢乐谷消息
     * @return          解析后的欢乐谷消息
     */
    public static HuanleguMessage parseMessage(Document document, boolean isResponse) {
        HuanleguMessage message = new HuanleguMessage();
        if (document == null) {
            Logger.info("huanlegu message failed: empty document.");
            return message;
        }
        Node head =  XPath.selectNode("/Trade/Head", document);
        message.version = StringUtils.trimToNull(XPath.selectText("./Version", head).trim());
        message.timeStamp = StringUtils.trimToNull(XPath.selectText("./TimeStamp", head));
        message.sequenceId = StringUtils.trimToNull(XPath.selectText("./SequenceId", head));
        message.sign = StringUtils.trimToNull(XPath.selectText("./Signed", head));

        if (isResponse) {
            message.statusCode = StringUtils.trimToNull(XPath.selectText("./StatusCode", head));
        }else {
            message.distributorId = StringUtils.trimToNull(XPath.selectText("./DistributorId", head));
            message.clientId = StringUtils.trimToNull(XPath.selectText("./ClientId", head));
        }

        if (isResponse && !"200".equals(message.statusCode)) {
            message.errorMsg = StringUtils.trimToNull(XPath.selectText("/Trade/Body/Message", document));
            Logger.error("huanlegu message failed: %s", message.errorMsg);
            return message;
        }

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
