package controllers;

import models.order.ECoupon;
import models.order.Order;
import models.order.OuterOrder;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: tanglq
 * Date: 13-1-31
 * Time: 下午2:52
 */
@With(OperateRbac.class)
public class QuickSearchs extends Controller {

    public static final String BASE_URL = Play.configuration.getProperty("application.baseUrl");

    public static void query(String q) {
        if (StringUtils.isBlank(q)) {
            renderText("请输入搜索参数");
        }
        String value = q.trim();
        boolean isOuterCoupon = isOuterCoupon(value);
        boolean isOuterOrder = isOuterOrder(value);

        if (value.length() == 11 && !isOuterCoupon && !isOuterOrder) {
            // 手机
            redirect(BASE_URL + "/orders?condition.searchKey=MOBILE&condition.searchItems=" + value);
        }
        if (value.length() == 10 && !isOuterCoupon && !isOuterOrder) {
            // 券号
            ECoupon eCoupon = ECoupon.find("eCouponSn=?", value).first();
            if (eCoupon != null) {
                redirect(BASE_URL + "/coupon_history?couponId=" + eCoupon.id);
            }
        }
        if (value.length() == 8 && !isOuterCoupon && !isOuterOrder) {
            // 订单号
            Order order = Order.find("orderNumber=?", value).first();
            if (order != null) {
                redirect(BASE_URL + "/orders/" + order.id);
            }
        }
        if (isOuterCoupon) {
            // 外部券号
            ECoupon eCoupon = ECoupon.find("partnerCouponId=?", value.substring(2, value.length())).first();
            if (eCoupon != null) {
                redirect(BASE_URL + "/coupon_history?couponId=" + eCoupon.id);
            }
        }
        if (isOuterOrder) {
            // 外部订单号
            OuterOrder outerOrder = OuterOrder.find("orderId=?", value.substring(2, value.length())).first();

            if (outerOrder != null) {
                if (outerOrder.ybqOrder == null) {
                    renderText("此订单是外部订单，订单信息已接收到，但是我们还未生成一百券的订单。");
                }
                redirect(BASE_URL + "/orders/" + outerOrder.ybqOrder.id);
            }
        }
        StringUtils.indexOfAny(value, new String[]{});

        renderText("没有找到" + value + "相关的信息，请关闭重试.");
    }

    public static boolean isOuterCoupon(String value) {
        return StringUtils.indexOfAny(value, new String[]{"WQ", "wq", "wQ", "Wq"}) >= 0;
    }

    public static boolean isOuterOrder(String value) {
        return StringUtils.indexOfAny(value, new String[]{"WD", "wd", "wD", "Wd"}) >= 0;
    }
}
