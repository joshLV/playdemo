package controllers;

import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.order.*;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * User: yan
 * Date: 13-3-22
 * Time: 下午4:36
 */
@With({SecureCAS.class})
@TargetOAuth(OAuthType.SINA)
public class WebSinaVouchers extends Controller {

    /**
     * 从新浪微博入口，展示页面
     *
     * @param productId 产品ID
     */
    @SkipCAS
    public static void showProduct(String productId, String source) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Collection<Shop> shops = goods.getShopList();

        String templatePath = "WebSinaVouchers/showProduct" + StringUtils.capitalize(StringUtils.trimToEmpty(source)) + ".html";

        render(templatePath, goods, shops, productId);
    }


    /**
     * 下单页面
     *
     * @param productId 商品ID
     */
    public static void showOrder(String productId) {
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
    public static void order(String productId, Long buyCount, String phone) {
        User user = SecureCAS.getUser();
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Validation.required("phone", phone);
        Validation.match("phone", phone, "^1\\d{10}$");

        Validation.required("buyCount", buyCount);
        if (Validation.hasErrors()) {
            render("WebSinaVouchers/showOrder.html", goods, productId, phone);
        }
        Resaler resaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);

        //创建订单
        Order order = Order.createConsumeOrder(user, resaler).save();
        OrderItems orderItems = null;
        try {
            orderItems = order.addOrderItem(goods, buyCount, phone, goods.getResalePrice(), goods.getResalePrice());
            orderItems.outerGoodsNo = productId;
            orderItems.save();
        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory is not enough!");
            error("库存不足！goodsId=" + goods.id);
        }

        order.deliveryType = DeliveryType.SMS;
        order.createAndUpdateInventory();
        order.discountPay = order.needPay;
        order.accountPay = BigDecimal.ZERO;
        order.save();

        user.updateMobile(phone);

        PaymentSource paymentSource = PaymentSource.findByCode("sina");
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);

        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress, "wap");

        PaymentJournal.savePayRequestJournal(
                order.orderNumber,
                order.description,
                order.discountPay.toString(),
                paymentSource.paymentCode,
                paymentSource.subPaymentCode,
                request.remoteAddress,
                form);
        render(form);

    }

    /**
     * 支付结果页面
     *
     * @param orderNumber 订单编号
     */

    public static void payResult(String orderNumber) {
    }


    /**
     * 检查是否从手机或除PC以外的设备访问页面
     *
     * @return
     */
    private static boolean isMobile() {
//        Http.Header headAgent = request.headers.get("user-agent");
//        String userAgent = headAgent.value();
//        for (String mobile : MOBILE_SPECIFIC_SUBSTRING) {
//            if (userAgent.contains(mobile)
//                    || userAgent.contains(mobile.toUpperCase())
//                    || userAgent.contains(mobile.toLowerCase())) {
//                return true;
//            }
//        }
//
//        return false;
        return true;
    }

    @Before
    private static void mobileCheck() {
        String url = request.url;
        if (isMobile()) {
            if (!url.startsWith(WEIBO_WAP)) {
                redirect(WEIBO_WAP + url.substring(6));
            }
        }
    }


    static final String WEIBO_WAP = "/weibo/wap";

    static final String[] MOBILE_SPECIFIC_SUBSTRING = {
            "iPad", "iPhone", "Android", "MIDP", "Opera Mobi",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile",
            "MSIEMobile", "Windows Phone", "HTC", "LG",
            "MOT", "Nokia", "Symbian", "Fennec",
            "Maemo", "Tear", "Midori", "armv",
            "Windows CE", "WindowsCE", "Smartphone", "240x320",
            "176x220", "320x320", "160x160", "webOS",
            "Palm", "Sagem", "Samsung", "SGH",
            "SIE", "SonyEricsson", "MMP", "UCWEB"};
}
