package models.jingdong.groupbuy.request;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-7
 */
public class SendOrderRequest implements JDMessage {
    public Long jdTeamId;       //京东团购ID
    public Long venderTeamId;   //合作伙伴团购ID
    public String mobile;       //手机
    public Date orderDate;      //下单时间
    public BigDecimal teamPrice;//购买价
    public Integer count;       //订购数量
    public BigDecimal origin;   //订单总额
    public Long jdOrderId;      //京东订单ID
    public Date payTime;        //付款时间
    public List<CouponRequest> coupons;       //券信息

    public SendOrderRequest(){
        coupons = new ArrayList<>();
    }

    @Override
    public boolean parse(Element root) {
        jdTeamId = Long.parseLong(root.elementTextTrim("JdTeamId"));
        venderTeamId = Long.parseLong(root.elementTextTrim("VenderTeamId"));
        mobile = root.elementTextTrim("Mobile");
        orderDate = new Date(Long.parseLong(root.elementTextTrim("OrderDate")));
        teamPrice = new BigDecimal(root.elementTextTrim("TeamPrice")).divide(new BigDecimal("100"));
        count = Integer.parseInt(root.elementTextTrim("Count"));
        origin = new BigDecimal(root.elementTextTrim("Origin")).divide(new BigDecimal("100"));
        jdOrderId = Long.parseLong(root.elementTextTrim("JdOrderId"));
        payTime = new Date(Long.parseLong(root.elementTextTrim("PayTime")));
        for(Element element : (List<Element>)root.element("Coupons").elements()){
            CouponRequest coupon = new CouponRequest();
            coupon.couponId = element.elementTextTrim("CouponId");
            coupon.couponPwd = element.elementTextTrim("CouponPwd");
            coupons.add(coupon);
        }
        return true;
    }
}
