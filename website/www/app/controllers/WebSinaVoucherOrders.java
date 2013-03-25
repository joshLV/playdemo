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
//@TargetOAuth(OAuthType.SINA)
public class WebSinaVoucherOrders extends Controller {
    /**
     * 下单页面
     */
    public static void index(String productId) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null) {
            notFound();
        }
        User user=SecureCAS.getUser();
        render(goods,productId,user);
    }

    /**
     * 确认下单
     */
    public static void confirm(String buyCount, String phone, String productId ,Long goodsId) {
        Goods goods = Goods.findUnDeletedById(goodsId);
        User user=SecureCAS.getUser();
        render(user,goods, productId, phone, buyCount);
    }

    /**
     * 创建订单
     */
    public static void create(Long buyCount, String phone, String productId ,Long goodsId) {
        User user = SecureCAS.getUser();

        Goods goods = Goods.findUnDeletedById(goodsId);
        //创建订单
        Order order = Order.createConsumeOrder(user.getId(), AccountType.CONSUMER).save();
        OrderItems orderItems = null;
        try {
            orderItems = order.addOrderItem(goods, buyCount, phone, goods.getResalePrice(), goods.getResalePrice());
            orderItems.outerGoodsNo = productId;
            orderItems.save();
        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory is not enough!");
        }

        order.deliveryType = DeliveryType.SMS;
        order.createAndUpdateInventory();
        order.accountPay = order.needPay;
        order.discountPay = BigDecimal.ZERO;

        order.payMethod = PaymentSource.getBalanceSource().code;
        order.payAndSendECoupon();
        order.save();

        redirect("/weibo/wap/payment_info/" + order.orderNumber);
    }

    public static void phone(Long goodsId, String productId,String buyCount){
        render("WebSinaVoucherOrders/bindPhone.html",goodsId,productId,buyCount);
    }
    /**
     * 发送验证码
     *
     * @param phone 手机
     */
    public static void sendValidCode(String phone) {
        //判断手机号码是否存在
        if (StringUtils.isBlank(phone)) {
            renderText("phone is empty!");
        }
        String validCode = RandomNumberUtil.generateSerialNumber(4);
        String comment = "您的验证码是" + validCode + ", 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码";
        SMSUtil.send(comment, phone, "0000");
        //保存手机和验证码
        Cache.set("validCode", validCode, "10mn");
        Cache.set("bind_phone", phone, "10mn");
        renderText("");
    }

    /**
     * 绑定手机
     *
     * @param phone 手机
     */
    public static void bindPhone(String phone, String validCode,Long goodsId,String productId,String buyCount) {
        Object objCode = Cache.get("validCode");
        Object objPhone = Cache.get("bind_phone");
        String cacheValidCode = objCode == null ? "" : objCode.toString();
        String cachePhone = objPhone == null ? "" : objPhone.toString();
        //判断验证码是否正确
        if (!StringUtils.normalizeSpace(cacheValidCode).equals(validCode)) {
            Validation.addError("msgInfo","验证码不正确！");
        }

        //判断手机是否正确
        if (!StringUtils.normalizeSpace(cachePhone).equals(phone)) {
            Validation.addError("msgInfo","手机输入不正确！");
        }
        if (Validation.hasErrors()){
            render(phone,validCode);
        }
        //更新用户基本信息手机
        User user = SecureCAS.getUser();
        user.updateMobile(phone);
        if (BindMobile.find("byMobileAndBindType", phone, MobileBindType.BIND_CONSUME).first() == null &&
                BindMobile.find("byBindTypeAndBindInfo", MobileBindType.BIND_CONSUME, String.valueOf(user.getId())).first() == null) {
            BindMobile bindMobile = new BindMobile(phone, MobileBindType.BIND_CONSUME);
            bindMobile.bindInfo = String.valueOf(user.getId());
            bindMobile.save();
        }

        Cache.delete("validCode");
        Cache.delete("bind_mobile");
        confirm(buyCount,phone,productId,goodsId);
    }
}
