package controllers;

import models.order.ECoupon;
import models.order.Order;
import org.jsoup.helper.StringUtil;
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
        if (StringUtil.isBlank(q)) {
            renderText("请输入搜索参数");
        }
        String value = q.trim();

        if (value.length() == 11) {
            // 手机
            redirect(BASE_URL + "/orders?condition.searchKey=MOBILE&condition.searchItems=" + value);
        }
        if (value.length() == 10) {
            // 券号
            ECoupon eCoupon = ECoupon.find("eCouponSn=?", value).first();
            if (eCoupon != null) {
                redirect(BASE_URL + "/coupon_history?couponId=" + eCoupon.id);
            }
        }
        if (value.length() == 8) {
            // 订单号
            Order order = Order.find("orderNumber=?", value).first();
            if (order != null) {
                redirect(BASE_URL + "/orders/" + order.id);
            }
        }

        renderText("没有找到" + value + "相关的信息，请关闭重试.");
    }
}
