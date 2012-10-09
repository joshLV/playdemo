package models.jingdong.groupbuy;

import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-7
 */
public class SendOrder implements JDMessage{
    public Integer jdTeamId;
    public Integer venderTeamId;
    public String mobile;
    public Date orderDate;
    public BigDecimal teamPrice;
    public Integer count;
    public BigDecimal origin;
    public Long jdOrderId;
    public Date payTime;
    List<Coupon> coupons;

    public SendOrder(){
        coupons = new ArrayList<>();
    }

    @Override
    public boolean parse(Element root) {
        jdTeamId = Integer.parseInt(root.elementTextTrim("JdTeamId"));
        venderTeamId = Integer.parseInt(root.elementTextTrim("VenderTeamId"));
        mobile = root.elementTextTrim("Mobile");
        orderDate = new Date(Long.parseLong(root.elementTextTrim("OrderDate")));
        teamPrice = new BigDecimal(root.elementTextTrim("TeamPrice")).divide(new BigDecimal("100"));
        count = Integer.parseInt(root.elementTextTrim("Count"));
        origin = new BigDecimal(root.elementTextTrim("Origin")).divide(new BigDecimal("100"));
        jdOrderId = Long.parseLong(root.elementTextTrim("JdOrderId"));
        payTime = new Date(Long.parseLong(root.elementTextTrim("PayTime")));
        for(Element element : (List<Element>)root.elements("Coupon")){
            Coupon coupon = new Coupon();
            coupon.couponId = element.elementTextTrim("CouponId");
            coupon.couponPwd = element.elementTextTrim("CouponPwd");
            coupons.add(coupon);
        }
        return false;
    }
}
