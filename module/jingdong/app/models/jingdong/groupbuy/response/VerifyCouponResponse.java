package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.util.Date;

/**
 * @author likang
 *         Date: 12-10-11
 */
public class VerifyCouponResponse implements JDMessage{
    public Long jdOrderId;
    public String couponId;
    public String couponPwd;
    public Date verifyTime;
    public int verifyResult;

    @Override
    public boolean parse(Element root) {
        jdOrderId = Long.parseLong(root.elementTextTrim("JdOrderId"));
        couponId = root.elementTextTrim("CouponId");
        couponPwd = root.elementTextTrim("CouponPwd");
        verifyTime = new Date(Long.parseLong(root.elementTextTrim("VerifyTime")));
        verifyResult = Integer.parseInt(root.elementTextTrim("VerifyResult"));

        return true;
    }
}
