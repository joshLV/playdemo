package models.jingdong.groupbuy.request;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.util.*;

/**
 * @author likang
 *         Date: 12-10-11
 */
public class SendOrderRefundRequest implements JDMessage{
    public Long jdOrderId;
    public String venderOrderId;
    public List<CouponRequest> coupons;

    public SendOrderRefundRequest(){
        coupons = new ArrayList<>();
    }

    @Override
    public boolean parse(Element root) {
        jdOrderId = Long.parseLong(root.elementTextTrim("JdOrderId"));
        venderOrderId = root.elementTextTrim("VenderOrderId");

        for(Element element : (List<Element>)root.elements("Coupons")){
            CouponRequest coupon = new CouponRequest();
            coupon.couponId = element.getTextTrim();
            coupons.add(coupon);
        }
        return true;
    }
}
