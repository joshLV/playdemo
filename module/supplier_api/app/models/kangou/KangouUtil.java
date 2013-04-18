package models.kangou;

import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.libs.XML;
import play.libs.XPath;
import util.ws.WebServiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 看购网接口工具类.
 *
 * 建立连接，发送请求.
 *
 * 本文描述了看购网与其他合作商获取卡密时所用的接口,接口开发采用 ASP.NET 技术,接口地址为 https://www.lookango.com/Service/CardAPI.aspx。
 * 为了保证数据的真实性所有接口采用 md5 验证。加密 key 由看购网提供。
 * 卡密采用 DES 加密。加密 key 由看购网提供。
 * 每个接口的的调用权限不同,调用权限由看购网提供。
 * 每个接口的数据 Return_Datas 为 XML 字符串。
 * 每个接口的输入参数为 1-50 位字符,采用 post 或 get 传递。接口前四个 参数相同为 HashCode, UserID,OrderID 和 FunctionID 。
 *    HashCode 为 md5 加密后的字符。
 *    UserID 为合作厂商 ID,由看购网提供。
 *    OrderID 为合作厂商的请求调用的订单 ID,不能重复,也可直接用 Guid。 FunctionID为GetCardKinds,GetCardEncodes,GetCardStatus,SetCardStatus,
 *    SetCardUseless 等,每个详细参数参见下面。
 * 例如: https://www.lookango.com/service/cardapi.aspx?FunctionID=GetCardEncodes&HashCode=2342asdfdfade3asf2342&UserID=11&OrderID=200802010001&CardKindID=9
 *
 * 提供的连接信息
 UserID=ffb20090-0901-2a7e-5a72-a361a2563af2
 MD5KEY=e125b66bd307e4fcc5a2e305367eb980

 FunctionID=<GetCardID><GetCardStatus><SetCardUseAndSend><GetCardKindCinemas><GetCardKinds>
 card URL= https://passport.kangou.cn/service/cardapi.aspx


 后台
 http://www.kangou.cn/login/ecard
 username=ShangHaiShiHui
 password=ShangHaiShiHui
 自己修改密码
 接入方使用查询后台进行订单明细对账，有漏单的再次发送请求，有多单的如测试的通知看购作废订单




 接口方式流程：
 1 先调2.2  获得票的种类（一般调用一次，也可以看购网直接提供,保存到Table中）
 2 调用2.15 获得种类对应的影院（最好每天更新一次,保存到Table中）
 3 用户下订单，并且支付成功。 支付成功后调用2.4+2.8 发送电子票。 （返回参数保存到Table中）

 注意的问题：

 订单号不能重复，对账以2.8为准，所以可以加负号，如2.4订单号-201101010001则2.8订单号201101010001
 我们没有测试服务器，直接正式下订单，在上线之前把测试订单作废即可。
 一个订单号下单没有成功，10分钟以后再次下单请求.
 */
public class KangouUtil {

    public static final String USER_ID = Play.configuration.getProperty("kangou.user_id", "ffb20090-0901-2a7e-5a72-a361a2563af2");
    public static final String MD5_KEY = Play.configuration.getProperty("kangou.md5_key",
            "e125b66bd307e4fcc5a2e305367eb980");
    public static final String URL = Play.configuration.getProperty("kangou.url",
            "https://passport.kangou.cn/service/cardapi.aspx");
    public static final String SUPPLIER_DOMAIN_NAME = "kangou";


    private static Map<String, Object> generateRequestParams(String functionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("UserId", USER_ID);
        params.put("FunctionID", functionId);
        return params;
    }
    private static String hashCode(String... args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
        }
        return DigestUtils.md5Hex(sb.toString());
    }

    /**
     * 获得一个卡密ID,但没有卡密,为不启用状态.
     *
     <summary>获得一个卡密ID,但没有卡密,为不启用状态</summary>
     <param name="HashCode">md5加密后的字符,加密顺序:key + UserId + OrderId+ CardKindID+ TicketCount +FunctionId </param>
     <param name="UserId">合作厂商ID</param>
     <param name="OrderId">合作厂商的内部的订单ID</param>
     <param name="FunctionID">调用的功能ID</param>
     <param name="CardKindID">卡密的种类</param>
     <param name="TicketCount">一个卡密包含的票数</param> <Return_Datas>
     <?xml version="1.0" encoding="utf-8" ?>
     <string xmlns="http://tempuri.org/">
     <LKG_CARDAPI_RETURN_HEADER> <OrderID>合作商请求的订单id</OrderID>
     <HashCode> md5加密后的字符,加密顺序:key +UserId + OrderId +
     FunctionId + Datas(Datas表示<Datas>...</Datas>之间的字符串,含
     <Datas>标记) </HashCode>
     </LKG_CARDAPI_RETURN_HEADER>
     <Datas>
     <Cards>
         <CardID>卡ID</CardID>
         <CardNumber>卡号</CardNumber> </Cards>
     </Datas>
     </string>
     </Return_Datas>

     * @param orderItems  订单项
     */
    public static KangouCard getCardId(ECoupon eCoupon) {
        if (eCoupon.goods.supplierGoodsId == null) {
            Logger.error("看购网商品必须设置外部商品ID，goods(id:%d, name:%s",
                    eCoupon.goods.id, eCoupon.goods.shortName);
            return null;
        }
        String functionId = "GetCardId";
        String ticketCount = "1";
        Map<String, Object> params = generateRequestParams(functionId);
        params.put("OrderId", eCoupon.id);
        params.put("CardKindID", eCoupon.goods.supplierGoodsId);
        params.put("TicketCount", ticketCount); // TODO: 确认一下是否一个券一张票
        params.put("HashCode", hashCode(MD5_KEY, USER_ID, eCoupon.id.toString(),
                eCoupon.goods.supplierGoodsId.toString(), ticketCount, functionId));

        Logger.info("KangouUtil.getCardId: orderId=%s, cardKindId=%s", eCoupon.id,
                eCoupon.goods.supplierGoodsId.toString());
        return doCallGetCardId(params);
    }

    private static KangouCard doCallGetCardId(Map<String, Object> params) {
        Document document = WebServiceRequest.url(URL).params(params).postXml();
        Logger.info("xml: \n%s", XML.serialize(document));

        // 检查是否出错, error message
        Node errorNode = XPath.selectNode("/string/Errors", document);
        if (errorNode != null) {
            String errorId = XPath.selectText("ErrorId", errorNode);
            String errorMessage = XPath.selectText("ErrorDescription", errorNode);
            Logger.info("found ERROR: %s, %s", errorId, errorMessage);
            return null;
        }

        KangouCard card = new KangouCard();
        card.cardId = XPath.selectText("/string/Datas/Cards/CardID", document);
        card.cardNumber = XPath.selectText("/string/Datas/Cards/CardNumber", document);
        return card;
    }

    /**
     * 启用卡,设置卡的状态为启用并把卡密发送短信或邮件给购买人.
     *
     <summary>启用卡,设置卡的状态为启用并把卡密发送短信或邮件给购买人 </summary>
     <param name="HashCode">md5加密后的字符,加密顺序:key + UserId + OrderId + CardID + Mobile + eMail + FunctionId </param>
     <param name="UserId">合作厂商ID</param>
     <param name="OrderId">合作厂商的内部的订单ID</param>
     <param name="CardID">卡密的ID</param>
     <param name="Mobile">手机号</param>
     <param name="eMail">邮箱</param>
     <param name="FunctionID">调用的功能ID</param> </param>
     <Return_Datas>
     <?xml version="1.0" encoding="utf-8" ?>
     <string xmlns="http://tempuri.org/">
     <LKG_CARDAPI_RETURN_HEADER>
     <OrderID>合作商请求的订单id</OrderID>
     <HashCode> md5加密后的字符,加密顺序:key +UserId + OrderId +
     FunctionId + Datas(Datas表示<Datas>...</Datas>之间的字符串,含
     <Datas>标记) </HashCode> </LKG_CARDAPI_RETURN_HEADER>
     <Datas>
     <Cards>
     <CardID>卡号</CardID>
     <CardStatus>卡状态 0未启用1启用9已使用</CardStatus> </Cards>
     </Datas>
     </string>
     </Return_Datas>
     *
     */
    public static KangouCardStatus setCardUseAndSend(ECoupon eCoupon) {
        String functionId = "SetCardUseAndSend";
        Map<String, Object> params = generateRequestParams(functionId);
        params.put("OrderId", eCoupon.order.orderNumber);
        params.put("CardID", eCoupon.eCouponSn);
        params.put("Mobile", eCoupon.orderItems.phone);
        params.put("HashCode", hashCode(MD5_KEY, USER_ID, eCoupon.order.orderNumber,
                eCoupon.eCouponSn, eCoupon.orderItems.phone, functionId));

        Logger.info("KangouUtil.setCardUseAndSend: orderId=%s, CardID=%s", eCoupon.order.orderNumber,
                eCoupon.eCouponSn);

        return doSetCardUseAndSend(params);
    }

    private static KangouCardStatus doSetCardUseAndSend(Map<String, Object> params) {
        Document document = WebServiceRequest.url(URL).params(params).postXml();
        Logger.info("xml: \n%s", XML.serialize(document));

        // 检查是否出错, error message
        Node errorNode = XPath.selectNode("/string/Errors", document);
        if (errorNode != null) {
            String errorId = XPath.selectText("ErrorId", errorNode);
            String errorMessage = XPath.selectText("ErrorDescription", errorNode);
            Logger.info("found ERROR: %s, %s", errorId, errorMessage);
            return null;
        }

        String strCardStatus = XPath.selectText("/string/Datas/Cards/CardStatus", document);
        if ("0".equals(strCardStatus)) {
            return KangouCardStatus.UNAVAIABLE;
        }
        if ("1".equals(strCardStatus)) {
            return KangouCardStatus.AVAIABLE;
        }
        return KangouCardStatus.USED;
    }

    /**
     * 获得卡的状态
     *
     <summary>获得卡的状态</summary>
     <param name="HashCode">md5加密后的字符,加密顺序:key + UserId + OrderId + CardID + FunctionId </param>
     <param name="UserId">合作厂商ID</param>
     <param name="OrderId">合作厂商的内部的订单ID</param>
     <param name="FunctionID">调用的功能ID</param>
     <param name="CardID">卡密的ID</param>
     </param>
     <Return_Datas>
     <?xml version="1.0" encoding="utf-8" ?>
     <string xmlns="http://tempuri.org/"> <LKG_CARDAPI_RETURN_HEADER> <OrderID>合作商请求的订单id</OrderID>
     <HashCode> md5加密后的字符,加密顺序:key +UserId + OrderId +
     FunctionId + Datas(Datas表示<Datas>...</Datas>之间的字符串,含
     <Datas>标记) </HashCode>
     </LKG_CARDAPI_RETURN_HEADER>
     <Datas>
     <CardStatus>
     <CardID>卡ID</CardID>
     <CardNumber>卡号</CardNumber>
     <CardStatus>卡状态 0未启用1启用9已使用</CardStatus>
     <CardDateEnd>有效截止日期</CardDateEnd>
     <TicketCount>卡能够兑换的总票数</TicketCount> <TicketRemainCount>卡能够兑换的剩余票数</TicketRemainCount>
     <TicketMonth>卡当月能够兑换的剩余票数</TicketMonth> <CardKind>卡的种类</CardKind>
     </CardStatus>
     </Datas>
     </string>
     </Return_Datas>
     */
    public static ECoupon getCardStatus(ECoupon eCoupon) {
        String functionId = "GetCardStatus";
        Map<String, Object> params = generateRequestParams(functionId);
        params.put("OrderId", eCoupon.order.orderNumber);
        params.put("CardID", eCoupon.eCouponSn);
        params.put("HashCode", hashCode(MD5_KEY, USER_ID, eCoupon.order.orderNumber,
                eCoupon.eCouponSn, functionId));

        Logger.info("KangouUtil.getCardStatus: orderId=%s, CardID=%s", eCoupon.order.orderNumber,
                eCoupon.eCouponSn);

        //返回更新过状态后的ECoupon
        return doCallGetCardStatus(eCoupon, params);
    }

    private static ECoupon doCallGetCardStatus(ECoupon eCoupon, Map<String, Object> params) {
        Document document = WebServiceRequest.url(URL).params(params).postXml();
        Logger.info("xml: \n%s", XML.serialize(document));

        // 检查是否出错, error message
        Node errorNode = XPath.selectNode("/string/Errors", document);
        if (errorNode != null) {
            String errorId = XPath.selectText("ErrorId", errorNode);
            String errorMessage = XPath.selectText("ErrorDescription", errorNode);
            Logger.info("found ERROR: %s, %s", errorId, errorMessage);
            return null;
        }

        String cardStatus = XPath.selectText("/string/Datas/CardStatus/CardStatus", document);

        if ("0".equals(cardStatus) || "1".equals(cardStatus)) {
            eCoupon.status = ECouponStatus.UNCONSUMED;
        }
        if ("9".equals(cardStatus)) {
            eCoupon.status = ECouponStatus.CONSUMED;
        }

        return eCoupon;
    }

    /**
     * 取消订单,设置卡的状态为废止.
     *
     <summary>取消订单,设置卡的状态为废止</summary>
     <param name="HashCode">md5加密后的字符,加密顺序:key + UserId + OrderId + CardID + FunctionId </param>
     <param name="UserId">合作厂商ID</param>
     <param name="OrderId">合作厂商的内部的订单ID</param> <param name="FunctionID">调用的功能ID</param>
     <param name="CardID">需要取消的CardID</param>
     <Return_Datas>
     <?xml version="1.0" encoding="utf-8" ?>
     <string xmlns="http://tempuri.org/"> <LKG_CARDAPI_RETURN_HEADER> <OrderID>合作商请求的订单id</OrderID>
     <HashCode> md5加密后的字符,加密顺序:key +UserId + OrderId +
     FunctionId + Datas(Datas表示<Datas>...</Datas>之间的字符串,含
     <Datas>标记) </HashCode> </LKG_CARDAPI_RETURN_HEADER>
     <Datas>
     <Cards>
     <CardID>卡号</CardID>
     <CardStatus>卡状态 0未启用1启用9已使用既废止</CardStatus> </Cards>
     </Datas>
     </string>
     </Return_Datas>
     */
    public static KangouCardStatus setCardUseless(ECoupon eCoupon) {

        String functionId = "SetCardUseAndSend";
        Map<String, Object> params = generateRequestParams(functionId);
        params.put("OrderId", eCoupon.order.orderNumber);
        params.put("CardID", eCoupon.eCouponSn);
        params.put("HashCode", hashCode(MD5_KEY, USER_ID, eCoupon.order.orderNumber,
                eCoupon.eCouponSn, functionId));

        Logger.info("KangouUtil.setCardUseless: orderId=%s, CardID=%s", eCoupon.order.orderNumber,
                eCoupon.eCouponSn);

        return doSetCardUseless(params);
    }

    private static KangouCardStatus doSetCardUseless(Map<String, Object> params) {
        Document document = WebServiceRequest.url(URL).params(params).postXml();
        Logger.info("xml: \n%s", XML.serialize(document));

        // 检查是否出错, error message
        Node errorNode = XPath.selectNode("/string/Errors", document);
        if (errorNode != null) {
            String errorId = XPath.selectText("ErrorId", errorNode);
            String errorMessage = XPath.selectText("ErrorDescription", errorNode);
            Logger.info("found ERROR: %s, %s", errorId, errorMessage);
            return null;
        }

        String strCardStatus = XPath.selectText("/string/Datas/Cards/CardStatus", document);
        if ("0".equals(strCardStatus)) {
            return KangouCardStatus.UNAVAIABLE;
        }
        if ("1".equals(strCardStatus)) {
            return KangouCardStatus.AVAIABLE;
        }
        if ("9".equals(strCardStatus)) {
            return KangouCardStatus.USED_REFUND;
        }
        return KangouCardStatus.USED;
    }

}
