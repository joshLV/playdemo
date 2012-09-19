package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.yihaodian.Util;
import models.order.*;
import models.resale.Resaler;
import models.sales.MaterialType;
import models.sales.Goods;
import models.yihaodian.groupbuy.YHDGroupBuyOrder;
import models.yihaodian.groupbuy.YHDGroupBuyUtil;
import models.yihaodian.groupbuy.response.*;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-9-12
 */
public class YHDGroupBuy extends Controller{
    public static String YHD_LOGIN_NAME = Play.configuration.getProperty("yihaodian.resaler_login_name", "yihaodian");
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1[3,5,8]\\d{9}$";

    /**
     * 接收一号店的新订单通知
     *
     */
    public static void orderInform() {
        TreeMap<String, String> params = YHDGroupBuyUtil.filterPlayParams(request.params.all());
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParam(params, "sign", "orderCode", "productId", "productNum",
                "orderAmount", "createTime", "paidTime", "userPhone", "productPrize", "groupId", "outerGroupId");
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        YHDGroupBuyOrder yhdGroupBuyOrder =  null;
        OrderInformResponse orderInformResponse = new OrderInformResponse();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode").trim()).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if(outerOrder == null){
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.YHD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.message = gson.toJson(params);
            outerOrder.save();
            try{ // 将订单写入数据库
                JPA.em().flush();
            }catch (Exception e){ // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                renderJSON(new YHDResponse(orderInformResponse));
            }
        }

        try{// 解析参数为对象
            yhdGroupBuyOrder = gson.fromJson(outerOrder.message, YHDGroupBuyOrder.class);
        }catch (JsonParseException e ){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "参数解析错误", null));
            renderJSON(new YHDResponse(errorResponse));
        }
        //检查订单数量
        if(yhdGroupBuyOrder.productNum <= 0){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "购买数量不能小于0", null));
        }
        //检查价格
        if(yhdGroupBuyOrder.productPrize.compareTo(new BigDecimal("0.1")) < 0){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "商品价格不能小于0.1元", null));
        }
        //检查订单金额
        if(yhdGroupBuyOrder.productPrize.multiply(new BigDecimal(yhdGroupBuyOrder.productNum))
                .compareTo(yhdGroupBuyOrder.orderAmount) != 0){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "订单金额不一致", null));
        }
        //检查手机号
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(yhdGroupBuyOrder.userPhone);
        if(!matcher.matches()){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "手机号码无效", null));
        }
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        try{
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //没拿到锁 放弃
            renderJSON(new YHDResponse(orderInformResponse));
            return;
        }
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY){
            Order ybqOrder = createYbqOrder(yhdGroupBuyOrder, errorResponse);
            if(errorResponse.errorCount > 0){
                renderJSON(new YHDResponse(orderInformResponse));
            }else  if(ybqOrder != null){
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
                orderInformResponse.updateCount = 1;
            }
        }else if(outerOrder.status == OuterOrderStatus.ORDER_DONE){
            orderInformResponse.updateCount = 1;
        }
        renderJSON(new YHDResponse(orderInformResponse));
    }

    /**
     * 处理一号店的查询消费券请求
     */
    public static void vouchersGet() {
        TreeMap<String, String> params = YHDGroupBuyUtil.filterPlayParams(request.params.all());
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode");
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode").trim()).first();
        if(outerOrder == null || outerOrder.ybqOrder == null){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.get.vouchers_not_found", "订单不存在,请检查 orderCode", null));
        }else if(!outerOrder.ybqOrder.orderNumber.equals(params.get("partnerOrderCode").trim())){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.get.vouchers_not_found", "订单关联错误,请检查 partnerOrderCode", null));
        }
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        VoucherInfoResponse voucherInfoResponse = new VoucherInfoResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        List<ECoupon> coupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        for(ECoupon coupon : coupons) {
            VoucherInfo voucherInfo = new VoucherInfo();
            voucherInfo.issueTime = dateFormat.format(new Date());
            voucherInfo.voucherCode = coupon.eCouponSn;
            if(coupon.effectiveAt != null){
                voucherInfo.voucherStartTime = dateFormat.format(coupon.effectiveAt);
            }else {
                voucherInfo.voucherStartTime = dateFormat.format(coupon.expireAt);
            }
            voucherInfo.voucherEndTime = dateFormat.format(coupon.expireAt);
            voucherInfo.voucherCount = 1;
            voucherInfoResponse.add(voucherInfo);
        }
        renderJSON(new YHDResponse(voucherInfoResponse));
    }

    /**
     * 消费券重新发送
     */
    public static void voucherResend() {
        TreeMap<String, String> params = YHDGroupBuyUtil.filterPlayParams(request.params.all());
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode",
                "voucherCode", "receiveMobile", "requestTime");
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode").trim()).first();
        ECoupon eCoupon = null;
        //检查订单存在与否
        if(outerOrder == null || outerOrder.ybqOrder == null){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "订单不存在,请检查 orderCode", null));
        }else if(!outerOrder.ybqOrder.orderNumber.equals(params.get("partnerOrderCode").trim())){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "订单关联错误,请检查 partnerOrderCode", null));
        }
        //检查请求时间
        if(errorResponse.errorCount == 0){
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            try{
                Date date = dateFormat.parse(params.get("requestTime").trim());
                if(Math.abs(date.getTime() - System.currentTimeMillis()) > 600000){
                    errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.time_invalid", "请求时间误差大于10分钟", null));
                }
            }catch (ParseException e){
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.time_invalid", "请求时间格式有误,请检查 requestTime", null));
            }
        }
        //检查券存在与否
        if(errorResponse.errorCount == 0){
            eCoupon = ECoupon.find("byOrderAndECouponSn", outerOrder.ybqOrder, params.get("voucherCode").trim()).first();
            if(eCoupon == null) {
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "消费券不存在", null));
            }
        }
        // 检查券的发送次数
        if(errorResponse.errorCount == 0){
            if(eCoupon.downloadTimes >= 3){
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "券发送次数已经到达3次上限", null));
            }
        }
        // 检查手机号
        if(errorResponse.errorCount == 0){
            Pattern pattern = Pattern.compile(PHONE_REGEX);
            Matcher matcher = pattern.matcher(params.get("receiveMobile").trim());
            if(!matcher.matches()){
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.resend.error", "手机号码错误", null));
            }
        }

        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }

        VoucherResendResponse voucherResendResponse = new VoucherResendResponse();

        eCoupon.downloadTimes = eCoupon.downloadTimes + 1;
        eCoupon.save();
//        ECoupon.send(eCoupon, params.get("receiveMobile").trim());

        voucherResendResponse.totalCount = 1;
        renderJSON(new YHDResponse(voucherResendResponse));
    }

    /**
     * 消费券退款
     */
    private static void voucherRefund() {
        TreeMap<String, String> params = YHDGroupBuyUtil.filterPlayParams(request.params.all());
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParam(params, "sign", "orderCode", "partnerOrderCode",
                "voucherCode", "receiveMobile", "requestTime");
        if(errorResponse.errorCount > 0){
            renderJSON(new YHDResponse(errorResponse));
        }
    }

    // 创建一百券订单
    private static Order createYbqOrder(YHDGroupBuyOrder yhdGroupBuyOrder, YHDErrorResponse errorResponse) {
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.error", "合作伙伴：一号店 不存在", null));
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        try {
            Goods goods = Goods.find("byId", Long.parseLong(yhdGroupBuyOrder.outerGroupId)).first();
            if(goods == null){
                Logger.info("goods not found: %s", yhdGroupBuyOrder.outerGroupId );
                errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.error", "找不到商品,请检查 outerGroupId", null));
                return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods,
                    yhdGroupBuyOrder.productNum,
                    yhdGroupBuyOrder.userPhone,
                    yhdGroupBuyOrder.productPrize,
                    yhdGroupBuyOrder.productPrize );
            uhuilaOrderItem.save();
            if(goods.materialType.equals(MaterialType.REAL)){
                ybqOrder.deliveryType = DeliveryType.SMS;
            }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.error", "库存不足", null));
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
}


