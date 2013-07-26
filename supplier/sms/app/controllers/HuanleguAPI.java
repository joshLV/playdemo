package controllers;

import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.order.ECoupon;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-6-25
 */
public class HuanleguAPI extends Controller {
    public static void couponConsumed(String xmlContent) {
        Logger.info("huanlegu coupon consumed: %s", xmlContent);
        HuanleguMessage message;

        String templatePath = "huanlegu/consumedResponse.xml";
        Template template = TemplateLoader.load(templatePath);

        Map<String, Object> responseParams = new HashMap<>();

        //空
        if (StringUtils.isBlank(xmlContent)) {
            responseParams.put("statusCode", "501");
            responseParams.put("message", "invalid param: xmlContent");

            Logger.error("huanlegu coupon consumed failed");
            String data = template.render(responseParams);
            renderXml(data);
            return;
        }
        //解析失败
        try {
            message = HuanleguUtil.parseMessage(xmlContent, false);
        }catch (Exception e) {
            responseParams.put("statusCode", "501");
            responseParams.put("message", "invalid param: xmlContent");

            Logger.error("huanlegu coupon consumed failed");
            String data = template.render(responseParams);
            renderXml(data);
            return;
        }

        String orderNumberStr = message.selectTextTrim("./OrderId");
        String[] orderNumberInfos = orderNumberStr.split("-");
//        Long supplierOrderId = Long.parseLong(message.selectTextTrim("./HvOrderId"));
        String supplierCoupon = message.selectTextTrim("./VoucherValue");
        Long consumeTimes = Long.parseLong(message.selectTextTrim("./ConsumeTimes"));
//        String consumeDate = message.selectTextTrim("./ConsumeDate");

        List<ECoupon> couponList = ECoupon.find("order.orderNumber = ? and supplierECouponPwd = ?", orderNumberInfos[0], supplierCoupon).fetch();
        for (ECoupon coupon : couponList) {
            String tmp[] = coupon.extra.split(";");
            if (tmp.length == 4) {
                coupon.extra = coupon.extra + ";" + consumeTimes;
                coupon.save();
            }
        }

        responseParams.put("statusCode", "200");
        responseParams.put("message", "OK");

        String data = template.render(responseParams);
        renderXml(data);
    }
}
