package models.taobao;

import com.google.gson.JsonObject;
import com.taobao.api.response.TradeGetResponse;
import com.uhuila.common.util.DateUtil;
import models.RabbitMQConsumerWithTx;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.ktv.KtvProductGoods;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvRoomType;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import models.supplier.Supplier;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-11-29
 */
@OnApplicationStart(async = true)
public class TaobaoCouponConsumer extends RabbitMQConsumerWithTx<TaobaoCouponMessage> {
    public static String PHONE_REGEX = "^1\\d{10}$";

//    public static Pattern skuPattern = Pattern.compile("(\\d+:\\d+);欢唱时间:(\\d+)点至(\\d+)点;日期:(\\d+)月(\\d+)日;?");

    public static Pattern skuDatePattern = Pattern.compile("(\\d+)月(\\d+)日");
    public static Pattern skuTimePattern = Pattern.compile("(\\d+)点至(\\d+)点");

    @Override
    protected int retries() {
        return 0;//抛异常不重试
    }

    @Override
    public void consumeWithTx(TaobaoCouponMessage taobaoCouponMessage) {
        OuterOrder outerOrder = OuterOrder.findById(taobaoCouponMessage.outerOrderId);
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            //订单接收到，开始创建一百券订单，并告诉淘宝我们的订单信息
            Logger.info("start taobao coupon consumer send order");
            if (outerOrder.ybqOrder != null) {
                Logger.info("our order already created");
            } else {
                //淘宝订单是否是等待发货
                if (!taobaoOrderReadyToSend(outerOrder)) {
                    outerOrder.status = OuterOrderStatus.ORDER_IGNORE;
                    outerOrder.save();
                    return;
                }
                if (send(outerOrder)) {
                    List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
                    Date now = new Date();
                    for (ECoupon coupon : couponList) {
                        coupon.partner = ECouponPartner.TB;
                        coupon.effectiveAt = now;//淘宝卖出去就直接可消费
                        coupon.save();
                    }
                    outerOrder.status = OuterOrderStatus.ORDER_DONE;
                    outerOrder.save();
                } else {
                    Logger.info("taobao coupon job failed: create our order failed %s", taobaoCouponMessage.outerOrderId);
                    throw new RuntimeException("taobao coupon job failed: create our order failed " + taobaoCouponMessage.outerOrderId);
                }
            }
        } else if (outerOrder.status == OuterOrderStatus.RESEND_COPY) {
            Logger.info("start taobao coupon consumer resend order");
            //重新发货的请求接收到，并先告诉淘宝我们要重新发货，然后再重新发货
            if (TaobaoCouponUtil.tellTaobaoCouponResend(outerOrder)) {
                outerOrder.ybqOrder.sendOrderSMS("淘宝重发短信");
                outerOrder.status = OuterOrderStatus.RESEND_SYNCED;
                outerOrder.save();
            } else {
                Logger.info("taobao coupon job failed: tell taobao coupon resend failed %s", taobaoCouponMessage.outerOrderId);
            }
        } else if (outerOrder.status == OuterOrderStatus.ORDER_DONE) {
            //我们发货了，但还没有通知淘宝成功，于是继续通知
            TaobaoCouponUtil.tellTaobaoCouponSend(outerOrder);
            outerOrder.save();
        }
    }

    //淘宝订单是否是等待发货
    private boolean taobaoOrderReadyToSend(OuterOrder outerOrder) {
        TradeGetResponse response = TaobaoCouponUtil.tradeInfo(Long.parseLong(outerOrder.orderId), "status");
        return response.getTrade().getStatus().equals("WAIT_SELLER_SEND_GOODS");
    }

    private boolean send(OuterOrder outerOrder) {
        Long outerIid;
        String mobile, sellerNick;
        Long num;
        Long taobaoOrderId;
        try {
            JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
            mobile = jsonObject.get("mobile").getAsString(); //买家手机号
            num = jsonObject.get("num").getAsLong();//购买的数量
            sellerNick = jsonObject.get("seller_nick").getAsString();//淘宝卖家用户名（旺旺号）
            outerIid = jsonObject.get("outer_iid").getAsLong();//商家发布商品时填写的外部商品ID
            taobaoOrderId = jsonObject.get("order_id").getAsLong();//淘宝的订单号
        } catch (Exception e) {
            Logger.error(e, "taobao coupon request failed: invalid params");
            return false;
        }
        if (!"券生活8".equals(sellerNick)) {
            Logger.info("taobao coupon request failed: invalid seller");
            return false;//暂时只发我们自己的店
        }

        //检查订单数量
        if (num <= 0 || !checkPhone(mobile)) {
            Logger.info("taobao coupon request failed: invalid params");
            return false;//解析错误
        }

        try {
            // 尝试申请一个行锁
            JPA.em().refresh(outerOrder, LockModeType.PESSIMISTIC_WRITE);
        } catch (PersistenceException e) {
            //没拿到锁 放弃
            Logger.info("taobao coupon request failed: concurrency request");
            return false;//解析错误
        }

        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            TradeGetResponse taobaoTrade = TaobaoCouponUtil.tradeInfo(taobaoOrderId, "orders.price,orders.num,orders.sku_properties_name");
            Order ybqOrder = createYbqOrder(outerIid, mobile, taobaoTrade);
            if (ybqOrder == null) {
                return false;//解析错误
            } else {
                outerOrder.status = OuterOrderStatus.ORDER_DONE;
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
            }
        } else {
            Logger.info("taobao coupon request failed: wrong order status");
            return false;
        }
        Logger.info("taobao coupon request create our order success");
        return true;
    }

    // 创建一百券订单
    private Order createYbqOrder(Long outerGroupId, String userPhone, TradeGetResponse taobaoTrade) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", Resaler.TAOBAO_LOGIN_NAME);
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        ybqOrder.save();
        Goods goods = ResalerProduct.getGoods(resaler, outerGroupId, OuterOrderPartner.TB);

        try {
            if (goods == null) {
                Logger.info("goods not found: %s", outerGroupId);
                return null;
            }
            List<com.taobao.api.domain.Order> orders = taobaoTrade.getTrade().getOrders();
            for (com.taobao.api.domain.Order order : orders) {
                OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, order.getNum(),
                        userPhone, new BigDecimal(order.getPrice()), new BigDecimal(order.getPrice()));
                uhuilaOrderItem.save();

                if (goods.getSupplierProperty(Supplier.KTV_SUPPLIER)) {
                    if (createSkuOrderInfo(uhuilaOrderItem, order, goods) == null) {
                        JPA.em().getTransaction().rollback();
                        return null;
                    }
                }
            }

            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.error("enventory not enough: goods.id=" + goods.id, e);
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

    /**
     * 创建淘宝sku订单信息
     */
    private KtvRoomOrderInfo createSkuOrderInfo(OrderItems uhuilaOrderItem, com.taobao.api.domain.Order order, Goods goods) {
        KtvProductGoods productGoods = KtvProductGoods.find("byGoods", goods).first();
        if (productGoods == null) {
            return null;
        }
        uhuilaOrderItem.faceValue = uhuilaOrderItem.salePrice;
        uhuilaOrderItem.originalPrice = uhuilaOrderItem.salePrice.multiply(goods.originalPrice.divide(goods.salePrice, RoundingMode.FLOOR)).setScale(2, BigDecimal.ROUND_HALF_UP);
        uhuilaOrderItem.save();

        Calendar calendar = Calendar.getInstance();
        Matcher matcher;
        KtvRoomOrderInfo roomOrderInfo = new KtvRoomOrderInfo();
        roomOrderInfo.goods = goods;
        roomOrderInfo.orderItem = uhuilaOrderItem;
        roomOrderInfo.dealAt = roomOrderInfo.createdAt;
        roomOrderInfo.shop = productGoods.shop;

        String[] properties = order.getSkuPropertiesName().split(";");
        for (String property : properties) {
            String[] map = property.split(":");
            if (map.length != 2) {
                Logger.error("parse taobao sku failed(1): " + property);
                return null;
            }
            switch (map[0]) {
                case "日期":
                    matcher = skuDatePattern.matcher(map[1]);
                    if (matcher.matches()) {
                        calendar.setTime(new Date());
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int skuMonth = Integer.parseInt(matcher.group(1));
                        //如果跨年了，要加一年
                        if (month == 12 && skuMonth == 1) {
                            year += 1;
                        }
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, skuMonth - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(2)));

                        roomOrderInfo.scheduledDay = DateUtils.truncate(calendar.getTime(), Calendar.DATE);
                    } else {
                        Logger.error("parse taobao sku failed(2): " + property);
                        return null;
                    }
                    break;
                case "欢唱时间":
                    matcher = skuTimePattern.matcher(map[1]);
                    if (matcher.matches()) {

                        int startTime = Integer.parseInt(matcher.group(1));
                        int endTime = Integer.parseInt(matcher.group(2));

                        roomOrderInfo.scheduledTime = startTime;
                        roomOrderInfo.duration = endTime - startTime + 1;
                    } else {
                        Logger.error("parse taobao sku failed(3): " + property);
                        return null;
                    }
                    break;
                default:
                    roomOrderInfo.roomType = KtvRoomType.getRoomTypeByTaobaoId(property);
                    break;
            }
        }
        return roomOrderInfo.save();
    }

    private boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    @Override
    protected Class getMessageType() {
        return TaobaoCouponMessage.class;
    }

    @Override
    protected String queue() {
        return TaobaoCouponMessageUtil.QUEUE_NAME;
    }
}
