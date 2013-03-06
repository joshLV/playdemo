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

        if (value.length() == 11 && checkNotOuterEcouponSNAndOrder(value, "ecouponSn") && checkNotOuterEcouponSNAndOrder(value, "orderNum")) {
            // 手机
            redirect(BASE_URL + "/orders?condition.searchKey=MOBILE&condition.searchItems=" + value);
        }
        if (value.length() == 10 && checkNotOuterEcouponSNAndOrder(value, "ecouponSn") && checkNotOuterEcouponSNAndOrder(value, "orderNum")) {
            // 券号
            ECoupon eCoupon = ECoupon.find("eCouponSn=?", value).first();
            if (eCoupon != null) {
                redirect(BASE_URL + "/coupon_history?couponId=" + eCoupon.id);
            }
        }
        if (value.length() == 8 && checkNotOuterEcouponSNAndOrder(value, "ecouponSn") && checkNotOuterEcouponSNAndOrder(value, "orderNum")) {
            // 订单号
            Order order = Order.find("orderNumber=?", value).first();
            if (order != null) {
                redirect(BASE_URL + "/orders/" + order.id);
            }
        }
        if (value.contains("WQ") || value.contains("wq") || value.contains("wQ") || value.contains("Wq")) {
            // 外部券号
            ECoupon eCoupon = ECoupon.find("partnerCouponId=?", value.substring(2, value.length())).first();
            if (eCoupon != null) {
                redirect(BASE_URL + "/coupon_history?couponId=" + eCoupon.id);
            }
        }

        if (value.contains("WD") || value.contains("wd") || value.contains("wD") || value.contains("wd")) {
            // 外部订单号
            OuterOrder outerOrder = OuterOrder.find("orderId=?", value.substring(2, value.length())).first();
            if (outerOrder != null) {
                redirect(BASE_URL + "/orders/" + outerOrder.ybqOrder.id);
            }
        }

        renderText("没有找到" + value + "相关的信息，请关闭重试.");
    }


    public static boolean checkNotOuterEcouponSNAndOrder(String value, String type) {
        if (type.equals("ecouponSn")) {
            return !value.contains("WQ") && !value.contains("wq") && !value.contains("wQ") && !value.contains("Wq");
        } else {
            return !value.contains("WD") && !value.contains("wd") && !value.contains("wD") && !value.contains("Wd");
        }
    }
}
