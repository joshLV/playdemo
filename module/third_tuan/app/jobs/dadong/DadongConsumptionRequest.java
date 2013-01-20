package jobs.dadong;

import models.order.OrderItems;

/**
 * 在订单生成后，大东票务的券不直接通过短信通道发送，而是通过调用大东票务的接口发送。
 *
 * 这里将作为一个普通的短信通道，调用后即返回对应的大东订单号，保存到ecoupon的partnerCouponId字段.
 * User: tanglq
 * Date: 13-1-20
 * Time: 下午1:48
 */
public class DadongConsumptionRequest {

    public void sendOrder(OrderItems orderItems) {

    }

}
