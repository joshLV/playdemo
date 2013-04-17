package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.util.DateUtil;
import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.consumer.User;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvRoomType;
import models.order.*;
import models.payment.PaymentFlow;
import models.payment.PaymentJournal;
import models.payment.PaymentUtil;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.classloading.enhancers.LocalvariablesNamesEnhancer;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.*;

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
        renderTemplate(templatePath, goods, shops, productId);
    }

    public static void jsonRoom(String productId, Date scheduleDay) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null || goods.status != GoodsStatus.ONSALE) {
            error("no goods!");
        }
        Collection<Shop> shops = goods.getShopList();
        Shop shop = shops.iterator().next();
        List<KtvRoom> roomList = KtvRoom.findByShop(shop);

        Map<String, Object> jsonParams = new HashMap<>();
        Map<String, String> roomsInfo = new HashMap<>();
        List<Map<String, String>> rooms = new ArrayList<>();
        for (KtvRoom ktvRoom : roomList) {
            roomsInfo.put("id", ktvRoom.id.toString());
            roomsInfo.put("name", ktvRoom.roomType.name);
            roomsInfo.put("type", ktvRoom.roomType.id.toString());
            rooms.add(roomsInfo);
        }

        List<KtvPriceSchedule> schedules = KtvPriceSchedule.getSchedulesByShop(scheduleDay, shop);
        Map<String, Object> scheduleInfo = new HashMap<>();
        List<Map<String, Object>> prices = new ArrayList<>();
        for (KtvPriceSchedule schedule : schedules) {
            scheduleInfo.put("startDay", schedule.startDay);
            scheduleInfo.put("endDay", schedule.endDay);
            scheduleInfo.put("weekday", schedule.useWeekDay);
            scheduleInfo.put("startTime", schedule.startTime);
            scheduleInfo.put("endTime", schedule.endTime);
            scheduleInfo.put("price", schedule.price);
            scheduleInfo.put("roomType", schedule.roomType);
            prices.add(scheduleInfo);
        }
        List<KtvRoomOrderInfo> scheduledRoomList = KtvRoomOrderInfo.findScheduledInfos(scheduleDay, shop);
        Map<String, Object> scheduleRoomInfo = new HashMap<>();
        List<Map<String, Object>> scheduleds = new ArrayList<>();
        for (KtvRoomOrderInfo orderInfo : scheduledRoomList) {
            scheduleRoomInfo.put("roomId", orderInfo.ktvRoom.id);
            scheduleRoomInfo.put("roomTime", orderInfo.scheduledTime);
            scheduleds.add(scheduleRoomInfo);
        }
        jsonParams.put("rooms", rooms);
        jsonParams.put("prices", prices);
        jsonParams.put("schedules", scheduleds);
        renderJSON(jsonParams);
    }

    /**
     * 下单页面
     *
     * @param productId 商品ID
     */
    public static void showOrder(String productId) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        if (goods == null || goods.status != GoodsStatus.ONSALE) {
            error("no goods!");
        }

        User user = SecureCAS.getUser();

        if ("1".equals(goods.getSupplier().getProperty(Supplier.KTV_SUPPLIER))) {
            renderTemplate("WebSinaVouchers/ktvorder.html", goods, productId, user);
        }
        render(goods, productId, user);
    }

    /**
     * 创建订单
     */
    public static void order(String productId, Long buyCount, String phone, Date scheduledDay, String source) {
        User user = SecureCAS.getUser();
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Validation.required("phone", phone);
        Validation.match("phone", phone, "^1\\d{10}$");
        if (!"1".equals(goods.getSupplier().getProperty(Supplier.KTV_SUPPLIER))) {
            Validation.required("buyCount", buyCount);
        }
        if (Validation.hasErrors()) {
            render("WebSinaVouchers/showOrder.html", goods, productId, phone);
        }
        Resaler resaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);
        if (resaler == null) {
            error("not found this resaler!");
        }
        //创建订单
        Order order = Order.createConsumeOrder(user, resaler).save();
        OrderItems orderItems = null;
        try {
            //页面根据包厢ID,取得该时间段的价格信息
            if ("1".equals(goods.getSupplier().getProperty(Supplier.KTV_SUPPLIER))) {
                for (String key : request.params.all().keySet()) {
                    if (key.startsWith("roomId")) {
                        String[] values = request.params.getAll(key);
                        String[] scheduledTimes = values[0].split("@");
                        for (String scheduledTime : scheduledTimes) {
                            KtvRoom ktvRoom = KtvRoom.findById(Long.valueOf(key.substring(6)));
                            KtvPriceSchedule ktvPriceSchedule = KtvPriceSchedule.findPrice(scheduledDay, scheduledTime, ktvRoom.roomType);
                            orderItems = order.addOrderItem(goods, Long.valueOf("1"), phone, ktvPriceSchedule.price, ktvPriceSchedule.price);
                            orderItems.outerGoodsNo = productId;
                            orderItems.save();
                            new KtvRoomOrderInfo(goods, orderItems, ktvRoom, ktvRoom.roomType, scheduledDay, scheduledTime).save();
                        }
                    }
                }
            } else {
                orderItems = order.addOrderItem(goods, buyCount, phone, goods.getResalePrice(), goods.getResalePrice());
                orderItems.outerGoodsNo = productId;
                orderItems.save();
            }

        } catch (NotEnoughInventoryException e) {
            Logger.info("inventory is not enough!");
            error("库存不足！goodsId=" + goods.id);
        }

        order.deliveryType = DeliveryType.SMS;
        order.generateOrderDescription();
        order.discountPay = order.needPay;
        order.accountPay = BigDecimal.ZERO;
        order.save();

        user.updateMobile(phone);

        PaymentSource paymentSource = PaymentSource.findByCode("sina");
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);

        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress, source);

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
     * 创建订单
     */
    public static void order1(String productId, Long buyCount, String phone, String source) {
        User user = SecureCAS.getUser();
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Validation.required("phone", phone);
        Validation.match("phone", phone, "^1\\d{10}$");

        Validation.required("buyCount", buyCount);
        if (Validation.hasErrors()) {
            render("WebSinaVouchers/showOrder.html", goods, productId, phone);
        }
        Resaler resaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);
        if (resaler == null) {
            error("not found this resaler!");
        }
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
        order.generateOrderDescription();
        order.discountPay = order.needPay;
        order.accountPay = BigDecimal.ZERO;
        order.save();

        user.updateMobile(phone);

        PaymentSource paymentSource = PaymentSource.findByCode("sina");
        PaymentFlow paymentFlow = PaymentUtil.getPaymentFlow(paymentSource.paymentCode);

        String form = paymentFlow.getRequestForm(order.orderNumber, order.description,
                order.discountPay, paymentSource.subPaymentCode, request.remoteAddress, source);

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
