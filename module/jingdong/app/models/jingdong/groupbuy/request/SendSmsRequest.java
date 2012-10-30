package models.jingdong.groupbuy.request;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-10-17
 */
public class SendSmsRequest implements JDMessage, Serializable {
    private static final long serialVersionUID = 7063122063919330652L;

    public String jdCouponId;
    public String venderCouponId;
    public String mobile;

    @Override
    public boolean parse(Element root) {
        jdCouponId = root.elementTextTrim("JdCouponId");
        venderCouponId = root.elementTextTrim("VenderCouponId");
        mobile = root.elementTextTrim("Mobile");
        return true;
    }
}
