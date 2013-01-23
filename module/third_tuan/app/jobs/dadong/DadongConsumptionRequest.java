package jobs.dadong;

import models.order.ECoupon;
import models.order.ECouponHistoryData;
import models.order.OrderItems;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 在订单生成后，大东票务的券不直接通过短信通道发送，而是通过调用大东票务的接口发送。
 * <p/>
 * 这里将作为一个普通的短信通道，调用后即返回对应的大东订单号，保存到ecoupon的partnerCouponId字段.
 * <p/>
 * User: tanglq
 * Date: 13-1-20
 * Time: 下午1:48
 */
public class DadongConsumptionRequest {

    public static void sendOrder(OrderItems orderItems) {

        if (!check(orderItems)) {
            return;
        }

        Supplier dadong = Supplier.findByDomainName("dadong");

        Template template = TemplateLoader.load("app/xml/template/dadong/ConsumptionRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("organCode", DadongProductsSyncRequest.ORGAN_CODE);
        args.put("orderItemId", orderItems.id);
        args.put("productId", orderItems.goods.supplierGoodsId);
        args.put("buyNumber", orderItems.buyNumber);
        args.put("phone", orderItems.phone);

        String xml = template.render(args);
        Map<String, Object> params = new HashMap<>();
        params.put("xml", xml);
        WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

        Document document = client.postXml("thirdtuan.dadang.Consumption", DadongProductsSyncRequest.URL, params, orderItems.id.toString(), orderItems.goods.id.toString(), orderItems.phone);

        Node rootNode = XPath.selectNode("business_trans", document);

        String thirdOrderId = XPath.selectText("sys_seq", rootNode);
        String resultId = XPath.selectText("result/id", rootNode);
        String resultComment = XPath.selectText("result/comment", rootNode);

        System.out.println("thirdOrderId = " + thirdOrderId);

        for (ECoupon ecoupon : orderItems.getECoupons()) {
            if (StringUtils.isNotBlank(thirdOrderId)) {
                ecoupon.partnerCouponId = thirdOrderId;
                ecoupon.save();
                ECouponHistoryData.newInstance(ecoupon).operator("MessageQ")
                        .remark("大东票务申请发券成功:" + resultId + resultComment)
                        .sendToMQ();
            } else {
                ECouponHistoryData.newInstance(ecoupon).operator("MessageQ")
                        .remark("大东票务申请发券失败:" + resultId + resultComment)
                        .sendToMQ();
            }
        }
    }

    public static boolean check(OrderItems orderItems) {
        Supplier dadong = Supplier.findByDomainName("dadong");
        if (dadong != null && dadong.id.equals(orderItems.goods.supplierId)) {
            return true;
        }
        return false;
    }

    public static boolean isResendTo(OrderItems orderItems) {
        if (orderItems.getECoupons() != null && orderItems.getECoupons().size() > 0) {
            ECoupon ecoupon = orderItems.getECoupons().get(0);
            if (StringUtils.isNotBlank(ecoupon.partnerCouponId)) {
                return true;
            }
        }
        return false;
    }
}
