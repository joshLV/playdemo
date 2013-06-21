package extension.order;

import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import util.extension.InvocationContext;

import java.util.List;

/**
 * 订单券短信内容上下文.
 */
public class OrderECouponSMSContext implements InvocationContext {

    public List<ECoupon> couponList;

    /**
     * 券内容
     */
    public String couponInfo;

    /**
     * 注意事项
     */
    public String notes;

    public boolean needSendSMS = true;

    /**
     * 有效期截止日期
     */
    public String expiredDate;

    private String smsContent;

    public OrderECouponSMSContext(List<ECoupon> couponList, String couponInfo, String notes, String expiredDate) {
        this.couponList = couponList;

        this.couponInfo = couponInfo;
        this.notes = notes;
        this.expiredDate = expiredDate;
        this.smsContent = null;
        this.needSendSMS = true;
    }

    public Order getOrder() {
        return getFirstCoupon().order;
    }

    public OrderItems getOrderItem() {
        return getFirstCoupon().orderItems;
    }

    public Goods getGoods() {
        return getFirstCoupon().goods;
    }

    public ECoupon getFirstCoupon() {
        return couponList.get(0);
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }
}
