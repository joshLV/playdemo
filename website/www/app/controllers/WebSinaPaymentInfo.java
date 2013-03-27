package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: yan
 * Date: 13-3-25
 * Time: 下午5:14
 */
@With({SecureCAS.class, WebsiteInjector.class})
//@TargetOAuth(OAuthType.SINA)
public class WebSinaPaymentInfo extends Controller {
    public static void index(String orderNumber) {
        //加载用户账户信息
        User user = SecureCAS.getUser();

        //加载订单信息
        Order order = Order.findOneByUser(orderNumber, user.getId(), AccountType.RESALER);

        if (order == null) {
            error("order not found");
            return;
        }

        List<ECoupon> coupons = order.eCoupons;
        render(coupons);
    }
}
