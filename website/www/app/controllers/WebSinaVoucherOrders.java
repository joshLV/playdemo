package controllers;

import com.uhuila.common.util.RandomNumberUtil;
import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sms.BindMobile;
import models.sms.MobileBindType;
import models.sms.SMSUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-3-22
 * Time: 下午5:18
 */
@With({SecureCAS.class, WebsiteInjector.class})
@TargetOAuth(OAuthType.SINA)
public class WebSinaVoucherOrders extends Controller {
    /**
     * 下单页面
     */
    public static void index(String productId) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null) {
            notFound();
        }
        User user = SecureCAS.getUser();
        render(goods, productId, user);
    }

    /**
     * 创建订单
     */
    public static void create(Long buyCount, String phone, String productId, Long goodsId) {
        User user = SecureCAS.getUser();

        Validation.match("phone", phone, "^1[3|4|5|8][0-9]\\d{4,8}$");
        if (Validation.hasErrors()) {
            render("/WebSinaVoucherOrders/index.html", phone);
        }
        Resaler resaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);

        Goods goods = Goods.findUnDeletedById(goodsId);
        //创建订单
        Order order = Order.createConsumeOrder(user, resaler).save();
        OrderItems orderItems = null;
        try {
            orderItems = order.addOrderItem(goods, buyCount, phone, goods.getResalePrice(), goods.getResalePrice());
            orderItems.outerGoodsNo = productId;
            orderItems.save();
        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory is not enough!");
            error("库存不足！goodsId=" + goodsId);
        }

        order.deliveryType = DeliveryType.SMS;
        order.createAndUpdateInventory();
        order.accountPay = order.needPay;
        order.discountPay = BigDecimal.ZERO;

        order.payMethod = PaymentSource.getBalanceSource().code;
        order.payAndSendECoupon();
        order.save();

        List<ECoupon> eCouponList = ECoupon.findByOrder(order);
        for (ECoupon coupon : eCouponList) {
            coupon.partner = ECouponPartner.SINA;
            coupon.save();
        }
        //更新用户手机
        user.updateMobile(phone);
        redirect("/weibo/wap/payment_info/" + order.orderNumber);
    }


}
