package jobs.dadong;

import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.OrderItems;
import models.supplier.Supplier;
import org.w3c.dom.Document;
import play.libs.XPath;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 重新发送券号到指定手机.
 * User: tanglq
 * Date: 13-1-20
 * Time: 下午1:31
 */
public class DadongErSendToRequest {

    public static void resend(OrderItems orderItems, String phone) {

        Supplier dadong = Supplier.findByDomainName("dadong");

        Template template = TemplateLoader.load("app/xml/template/dadong/ErSendToRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("organCode", DadongProductsSyncRequest.ORGAN_CODE);
        args.put("orderItemId", orderItems.id);
        args.put("phone", phone);

        String xml = template.render(args);
        Map<String, Object> params = new HashMap<>();
        params.put("xml", xml);
        WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

        Document document = client.postXml("thirdtuan.dadang.ErSendTo", DadongProductsSyncRequest.URL, params,
                orderItems.id.toString(), orderItems.goods.id.toString(), phone);
        String resultId = XPath.selectText("business_trans/result/id", document);
        String resultComment = XPath.selectText("business_trans/result/comment", document);
        String remark = "大东票务重发券";
        if (!orderItems.phone.equals(phone)) {
            remark += " 发至新手机" + phone;
        }
        for (ECoupon ecoupon : orderItems.getECoupons()) {
            new CouponHistory(ecoupon, "MessageQ", remark + ":" + resultId + resultComment,
                    ecoupon.status, ecoupon.status, null).save();
        }
    }

}
