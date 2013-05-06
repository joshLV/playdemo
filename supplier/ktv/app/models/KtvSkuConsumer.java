//package models;
//
//import com.taobao.api.ApiException;
//import com.taobao.api.DefaultTaobaoClient;
//import com.taobao.api.TaobaoClient;
//import com.taobao.api.request.ItemSkuAddRequest;
//import com.taobao.api.request.ItemSkuUpdateRequest;
//import com.taobao.api.response.ItemSkuAddResponse;
//import com.taobao.api.response.ItemSkuUpdateResponse;
//import models.accounts.AccountType;
//import models.ktv.KtvPriceSchedule;
//import models.oauth.OAuthToken;
//import models.oauth.WebSite;
//import models.resale.Resaler;
//import models.taobao.KtvSkuMessage;
//import models.taobao.KtvSkuMessageUtil;
//import models.taobao.OperateType;
//import org.apache.commons.lang.StringUtils;
//import org.bouncycastle.crypto.engines.CAST5Engine;
//import play.Logger;
//import play.Play;
//import play.jobs.OnApplicationStart;
//
///**
// * User: yan
// * Date: 13-5-6
// * Time: 下午3:18
// */
//@OnApplicationStart(async = true)
//public class KtvSkuConsumer extends RabbitMQConsumerWithTx<KtvSkuMessage> {
//    // 淘宝电子凭证的secret
//    public static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
//    public static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");
//    public static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
//
//    @Override
//    public void consumeWithTx(KtvSkuMessage message) {
//        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
//        //找到淘宝的token
//        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
//        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
//
//        switch ((OperateType) message.getParams().get("type")) {
//            case ADD:
//                itemSkuAdd(message, taobaoClient, token);
//                break;
//            case EDIT:
//                itemSkuEdit(message, taobaoClient, token);
//                break;
//            default:
//                break;
//        }
//
//
//    }
//
//    /**
//     * 编辑ktv sku
//     */
//    private void itemSkuEdit(KtvSkuMessage message, TaobaoClient taobaoClient, OAuthToken token) {
//        ItemSkuUpdateRequest req = new ItemSkuUpdateRequest();
//        req.setNumIid(Long.valueOf(message.partnerProductId));
//        req.setProperties("$时间:12：00至15：00;");
//        req.setQuantity(3L);
//        req.setPrice("9");
//        req.setOuterId("123456");
//        try {
//            ItemSkuUpdateResponse response = taobaoClient.execute(req, token.accessToken);
//            if (StringUtils.isBlank(response.getErrorCode())) {
//
//            }
//        } catch (ApiException e) {
//            Logger.info(e, "update ktv sku to taobao failed");
//        }
//    }
//
//    /**
//     * 添加ktv sku
//     */
//    private void itemSkuAdd(KtvSkuMessage message, TaobaoClient taobaoClient, OAuthToken token) {
//
//        ItemSkuAddRequest req = new ItemSkuAddRequest();
//        req.setNumIid(Long.valueOf(message.partnerProductId));
//
//        req.setProperties(message.getParams().get("properties").toString());
//        req.setQuantity(Long.valueOf(message.getParams().get("roomCount").toString()));
//        req.setPrice(String.valueOf(message.getParams().get("price")));
//        try {
//            ItemSkuAddResponse response = taobaoClient.execute(req, token.accessToken);
//            if (StringUtils.isBlank(response.getErrorCode())) {
//
//            }
//        } catch (ApiException e) {
//            Logger.info(e, "add sku to taobao failed");
//        }
//    }
//
//    private String getProperties(KtvPriceSchedule schedule) {
//        StringBuilder properties = new StringBuilder();
//        return properties.toString();
//    }
//
//    @Override
//    protected Class getMessageType() {
//        return KtvSkuMessage.class;
//    }
//
//    @Override
//    protected String queue() {
//        return KtvSkuMessageUtil.QUEUE_NAME;
//    }
//}
