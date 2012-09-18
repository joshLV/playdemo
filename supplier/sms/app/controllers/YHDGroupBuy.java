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

/**
 * @author likang
 *         Date: 12-9-12
 */
public class YHDGroupBuy extends Controller{
    public static String YHD_LOGIN_NAME = Play.configuration.getProperty("yihaodian.resaler_login_name", "yihaodian");
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    /**
     * 一号店API入口
     */
    public static void index(String sign){
        TreeMap<String, String> params = YHDGroupBuyUtil.filterPlayParams(request.params.all());
        //检查系统参数
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParamBlank(params, true,
                "checkCode", "merchantId", "sign", "erp", "erpVer", "format", "ver", "method");
        if(errorResponse.errorCount > 0){
            renderJSON(errorResponse);
        }
        //检查参数签名
        String mySign = YHDGroupBuyUtil.md5Signature(params, Util.SECRET_KEY);
        if(!mySign.equals(sign)){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "sign不匹配", ""));
            renderJSON(errorResponse);
        }
        params.put("sign", sign);//将sign重新塞回去
        String method = params.get("method");
        if("yhd.group.buy.order.inform".equals(method)){
            orderInform(params);
        }else if("yhd.group.buy.vouchers.get".equals(method)){
            vouchersGet(params);
        }

    }

    /**
     * 接收一号店的新订单通知
     *
     * @param params 一号店通知的参数
     */
    private static void orderInform(Map<String, String> params) {
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParamBlank(params, false, "orderCode", "productId", "productNum",
                "orderAmount", "createTime", "paidTime", "userPhone", "productPrice", "groupId", "outerGroupId");
        if(errorResponse.errorCount > 0){
            renderJSON(errorResponse);
        }
        YHDGroupBuyOrder yhdGroupBuyOrder =  null;
        OrderInformResponse orderInformResponse = new OrderInformResponse();
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode")).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if(outerOrder == null){
            outerOrder = new OuterOrder();
            outerOrder.partner = OuterOrderPartner.YHD;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.message = gson.toJson(params);
            try{ // 将订单写入数据库
                JPA.em().flush();
            }catch (Exception e){ // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，则放弃
                renderJSON(orderInformResponse);
            }
        }

        try{// 解析参数为对象
            yhdGroupBuyOrder = gson.fromJson(outerOrder.message, YHDGroupBuyOrder.class);
        }catch (JsonParseException e ){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.order.inform.param_invalid", "参数解析错误", ""));
        }
        if(errorResponse.errorCount > 0) {
            renderJSON(errorResponse);
        }

        try{
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //没拿到锁 放弃
            renderJSON(orderInformResponse);
            return;
        }
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY){
            Order ybqOrder = createYbqOrder(yhdGroupBuyOrder);
            if(ybqOrder != null){
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
                List<ECoupon> coupons = ECoupon.find("byOrder", ybqOrder).fetch();
                orderInformResponse.updateCount = coupons.size();
            }
        }else if(outerOrder.status == OuterOrderStatus.ORDER_DONE){
            orderInformResponse.updateCount = yhdGroupBuyOrder.productNum;
        }
        renderJSON(orderInformResponse);
    }

    /**
     * 处理一号店的查询消费券请求
     * @param params 一号店参数
     */
    private static void vouchersGet(Map<String, String> params) {
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParamBlank(params, false, "orderCode", "partnerOrderCode");
        if(errorResponse.errorCount > 0){
            renderJSON(errorResponse);
        }
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode")).first();
        if(outerOrder == null || outerOrder.ybqOrder == null){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.get.vouchers_not_found", "消费券列表信息不存在,请检查 orderCode", ""));
            renderJSON(errorResponse);
            return;
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
        renderJSON(voucherInfoResponse);
    }

    private static void voucherResend(Map<String, String> params) {
        //检查应用级参数
        YHDErrorResponse errorResponse = YHDGroupBuyUtil.checkParamBlank(params, false, "orderCode", "partnerOrderCode",
                "voucherCode", "receiveMobile", "requestTime");
        if(errorResponse.errorCount > 0){
            renderJSON(errorResponse);
        }

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.YHD, params.get("orderCode")).first();
        if(outerOrder == null || outerOrder.ybqOrder == null){
            errorResponse.addErrorInfo(new YHDErrorInfo("yhd.group.buy.vouchers.get.vouchers_not_found", "消费券列表信息不存在,请检查 orderCode", ""));
            renderJSON(errorResponse);
            return;
        }

        VoucherResendResponse voucherResendResponse = new VoucherResendResponse();
        ECoupon eCoupon = ECoupon.find("byOrderAndECouponSn", outerOrder.ybqOrder, params.get("voucherCode")).first();
        if(eCoupon == null){
            renderJSON(voucherResendResponse);
            return;
        }
        if(ECoupon.sendUserMessage(eCoupon.getId())){
            voucherResendResponse.totalCount = 1;
        }
        renderJSON(voucherResendResponse);
    }



    // 创建一百券订单
    private static Order createYbqOrder(YHDGroupBuyOrder yhdGroupBuyOrder) {
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        try {
            Goods goods = Goods.find("byId", yhdGroupBuyOrder.outerGroupId).first();
            if(goods == null){
                Logger.info("goods not found: %s", yhdGroupBuyOrder.outerGroupId );
                return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods,
                    yhdGroupBuyOrder.productNum,
                    yhdGroupBuyOrder.userPhone,
                    yhdGroupBuyOrder.productPrice,
                    yhdGroupBuyOrder.productPrice );
            uhuilaOrderItem.save();
            if(goods.materialType.equals(MaterialType.REAL)){
                ybqOrder.deliveryType = DeliveryType.SMS;
            }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
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


