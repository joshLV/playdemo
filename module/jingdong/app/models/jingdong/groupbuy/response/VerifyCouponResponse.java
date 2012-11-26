package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-10-11
 */
public class VerifyCouponResponse implements JDMessage, Serializable {
    private static final long serialVersionUID = 7063222463915330612L;

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
        String elementVerifyTime = root.elementTextTrim("VerifyTime");
        // 已验证时这个值为空.
        if (StringUtils.isNotBlank(elementVerifyTime)) {
            verifyTime = new Date(Long.parseLong(elementVerifyTime));
        }
        verifyResult = Integer.parseInt(root.elementTextTrim("VerifyResult"));

        return true;
    }
}
