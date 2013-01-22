package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.order.*;
import models.resale.Resaler;
import models.sales.MaterialType;
import models.sales.Goods;
import models.yihaodian.YHDErrorInfo;
import models.yihaodian.YHDUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-9-12
 */
public class YHDGroupBuy extends Controller {
    public static String YHD_LOGIN_NAME = Resaler.YHD_LOGIN_NAME;
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1\\d{10}$";

    /**
     * 接收一号店的新订单通知
     */
    public static void orderInform(Long orderCode, Long productId, Integer productNum, BigDecimal orderAmount,
                                   Date createTime, Date paidTime, String userPhone, BigDecimal productPrice,
                                   String outerGroupId) {
        TreeMap<String, String> params = YHDUtil.filterPlayParams(request.params.allSimple());
        List<YHDErrorInfo> errorInfoList = YHDUtil.checkParam(params, "sign", "orderCode", "productId", "productNum",
                "orderAmount", "createTime", "paidTime", "userPhone", "productPrice", "outerGroupId");
        int totalCount = 0;
        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.YHD, orderCode).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.YHD;
            outerOrder.orderId = orderCode;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.message = gson.toJson(params);
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_orderCode_exist", "重复的请求", null));
                finish(errorInfoList, totalCount);
            }
        }
        Logger.info("yihaodian: %s", outerOrder.message);

        //检查订单数量
        if (productNum <= 0) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "购买数量不能小于0", null));
        }
        //检查价格
        if (productPrice.compareTo(BigDecimal.ZERO) < 0) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_productPrice_invalid", "商品价格不能小于0元", null));
        }
        //检查订单金额
        if (productPrice.multiply(new BigDecimal(productNum)).compareTo(orderAmount) != 0) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_orderAmount_invalid", "订单金额不一致", null));
        }
        //检查手机号
        if (!checkPhone(userPhone)) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "手机号码无效", null));
        }
        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        try {
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //没拿到锁 放弃
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_orderCode_exist", "重复的请求", null));
            finish(errorInfoList, totalCount);
            return;
        }
        //保存订单编号
        if (outerOrder.orderId == null) {
            outerOrder.orderId = orderCode;
            outerOrder.save();
        }
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = outerOrder.ybqOrder;
            if (ybqOrder == null) {
                ybqOrder = createYbqOrder(outerGroupId, productPrice, productNum, userPhone, errorInfoList);
            }
            if (errorInfoList.size() > 0) {
                finish(errorInfoList, totalCount);
            } else if (ybqOrder != null) {
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.message = gson.toJson(params);
                outerOrder.save();
                renderArgs.put("data", "\"updateCount\": 1");
                totalCount = 1;
                finish(errorInfoList, totalCount);
            }
        } else if (outerOrder.status != OuterOrderStatus.ORDER_CANCELED) {
            //目前（12-09-20）一号店要求，如果订单不是order_copy或者order_canceled 那么其他状态应该都返回给一号店失败
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_orderCode_exist", "订单已存在,重复的订单请求", null));
            finish(errorInfoList, totalCount);
        }
    }

    /**
     * 处理一号店的查询消费券请求
     */
    public static void vouchersGet() {
        TreeMap<String, String> params = YHDUtil.filterPlayParams(request.params.allSimple());
        List<YHDErrorInfo> errorInfoList = YHDUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode");
        int totalCount = 0;
        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.YHD, Long.valueOf(params.get("orderCode"))).first();
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.vouchers.get_orderCode_invalid", "订单不存在,请检查 orderCode", null));
        } else if (!outerOrder.ybqOrder.orderNumber.equals(params.get("partnerOrderCode"))) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.vouchers.get_partnerOrderCode_invalid", "订单关联错误,请检查 partnerOrderCode", null));
        }
        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        List<ECoupon> coupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        Template template = TemplateLoader.load("yihaodian/groupbuy/response/vouchersGet.json");
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("coupons", coupons);
        renderArgs.put("data", template.render(templateParams));

        totalCount = coupons.size();
        finish(errorInfoList, totalCount);
    }

    /**
     * 消费券重新发送
     */
    public static void voucherResend() {
        TreeMap<String, String> params = YHDUtil.filterPlayParams(request.params.allSimple());
        List<YHDErrorInfo> errorInfoList = YHDUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode",
                "voucherCode", "receiveMobile", "requestTime");
        int totalCount = 0;
        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.YHD, Long.valueOf(params.get("orderCode"))).first();
        ECoupon eCoupon = null;
        //检查订单存在与否
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.voucher.resend_orderCode_invalid", "订单不存在,请检查 orderCode", null));
        } else if (!outerOrder.ybqOrder.orderNumber.equals(params.get("partnerOrderCode"))) {
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.voucher.resend_partnerOrderCode_invalid", "订单关联错误,请检查 partnerOrderCode", null));
        }
        //检查请求时间
        if (errorInfoList.size() == 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date date = dateFormat.parse(params.get("requestTime"));
                if (Math.abs(date.getTime() - System.currentTimeMillis()) > 600000) {
                    errorInfoList.add(new YHDErrorInfo("yhd.group.buy.voucher.resend_requestTime_invalid", "请求时间误差大于10分钟", null));
                }
            } catch (ParseException e) {
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.vouchers.resend.time_invalid", "请求时间格式有误,请检查 requestTime", null));
            }
        }
        //检查券存在与否
        if (errorInfoList.size() == 0) {
            eCoupon = ECoupon.find("byOrderAndECouponSn", outerOrder.ybqOrder, params.get("voucherCode")).first();
            if (eCoupon == null) {
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.voucher.resend_voucherCode_invalid", "消费券不存在", null));
            }
        }
        // 检查券的发送次数
        if (errorInfoList.size() == 0) {
            if (eCoupon.downloadTimes <= 0) {
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.voucher.resend_requestNumber_invalid", "券发送次数已经到达3次上限", null));
            }
        }
        // 检查手机号
        if (errorInfoList.size() == 0) {
            if (!checkPhone(params.get("receiveMobile"))) {
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "手机号码格式错误", null));
            }
        }

        if (errorInfoList.size() > 0) {
            finish(errorInfoList, totalCount);
        }

        eCoupon.downloadTimes = eCoupon.downloadTimes - 1;
        eCoupon.save();
        ECoupon.send(eCoupon, params.get("receiveMobile"));

        totalCount = 1;
        finish(errorInfoList, totalCount);
    }

    /**
     * 消费券退款
     */
    private static void voucherRefund() {
        TreeMap<String, String> params = YHDUtil.filterPlayParams(request.params.allSimple());
        List<YHDErrorInfo> errorInfoList = YHDUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode",
                "voucherCode", "receiveMobile", "requestTime");
    }

    // 创建一百券订单
    private static Order createYbqOrder(String outerGroupId, BigDecimal productPrize,
                                        Integer productNum, String userPhone, List<YHDErrorInfo> errorInfoList) {
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.error", "合作伙伴：一号店 不存在", null));
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            Long goodsId = null;
            try {
                goodsId = Long.parseLong(outerGroupId);
            } catch (NumberFormatException e) {
                Logger.info("goodsId is not long: %s", outerGroupId);
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.error", "找不到商品,请检查 outerGroupId", null));
                return null;
            }
            Goods goods = Goods.find("byId", goodsId).first();
            if (goods == null) {
                Logger.info("goods not found: %s", outerGroupId);
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.error", "找不到商品,请检查 outerGroupId", null));
                return null;
            }
            if (goods.originalPrice.compareTo(productPrize) > 0) {
                Logger.info("invalid yhd productPrice: %s", productPrize);
                errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform_productPrize_invalid", "商品价格不合法，拒绝订单", null));
                return null;
            }

            OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(
                    goods, productNum, userPhone, productPrize, productPrize);
            uhuilaOrderItem.save();
            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            errorInfoList.add(new YHDErrorInfo("yhd.group.buy.order.inform.error", "库存不足", null));
            JPA.em().getTransaction().rollback();
            return null;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        return ybqOrder;
    }

    private static void finish(List<YHDErrorInfo> errorInfoList, int totalCount) {
        renderArgs.put("errorInfoList", errorInfoList);
        renderArgs.put("totalCount", totalCount);
        renderTemplate("yihaodian/groupbuy/response/main.json");
    }

    private static boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}


