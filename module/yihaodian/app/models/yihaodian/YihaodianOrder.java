package models.yihaodian;

import org.dom4j.Element;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-8-30
 */
@Entity
@Table(name = "yihaodian_order")
public class YihaodianOrder extends Model{
    @Column(name = "order_id", unique = true)
    public Long orderId;                //订单编号

    @Column(name = "order_code", unique = true)
    public String orderCode;            //订单编码

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    public OrderStatus orderStatus;

    @Column(name = "order_amount")
    public BigDecimal orderAmount;      //订购金额（实际支付金额，包括运费）

    @Column(name = "product_amount")
    public BigDecimal productAmount;    //产品总额

    @Column(name = "order_create_time")
    public Date orderCreateTime;        //订单创建日期

    @Column(name = "order_delivery_fee")
    public BigDecimal orderDeliveryFee; //运费

    @Column(name = "order_need_invoice")
    public Integer orderNeedInvoice;    //发票需要情况：0 不需要 1 旧版普通 2 新版普通 3 增值税发票

    @Column(name = "update_time")
    public Date updateTime;             //更新时间

    @Column(name = "created_at")
    public Date createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_flag")
    public JobFlag jobFlag;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    public YihaodianOrder(){
        this.createdAt = new Date();
        this.jobFlag = JobFlag.SEND_COPY;
        this.orderItems = new ArrayList<>();

    }

    //---------- 补充信息 -----

    @Column(name = "express_nbr")
    public String expressNbr;

    @Column(name = "delivery_supplier_id")
    public Integer deliverySupplierId;

    @Column(name = "delivery_date")
    public Date deliveryDate;

    @Column(name = "receive_date")
    public Date receiveDate;

    @Column(name = "good_receiver_name")
    public String goodReceiverName;

    @Column(name = "good_receiver_address")
    public String goodReceiverAddress;

    @Column(name = "good_receiver_province")
    public String goodReceiverProvince;

    @Column(name = "good_receiver_city")
    public String goodReceiverCity;

    @Column(name = "good_receiver_country")
    public String goodReceiverCountry;

    @Column(name = "good_receiver_postcode")
    public String goodReceiverPostcode;

    @Column(name = "good_receiver_phone")
    public String goodReceiverPhone;

    @Column(name = "good_receiver_mobile")
    public String goodReceiverMobile;

    @Column(name = "fost_rebate")
    public Float fostRebate;

    @Column(name = "delivery_method_type")
    public Integer deliveryMethodType;

    @Column(name = "create_pay_time")
    public Date createPayTime;

    @Column(name = "payment_confirm_date")
    public Date paymentConfirmDate;

    @Column(name = "promotion_discount")
    public BigDecimal promotionDiscount;

    @Column(name = "pay_service_type")
    public Integer payServiceType;

    @Column(name = "recompens_points")
    public Float recompensPoints;

    // 订单摘要解析器
    public static Parser<YihaodianOrder> parser = new Parser<YihaodianOrder>() {
        @Override
        public YihaodianOrder parse(Element node) {
            YihaodianOrder order = new YihaodianOrder();
            order.orderId = Long.parseLong(node.elementTextTrim("orderId"));
            order.orderCode = node.elementTextTrim("orderCode");
            order.orderStatus = OrderStatus.valueOf(node.elementTextTrim("orderStatus"));
            order.orderAmount = new BigDecimal(node.elementTextTrim("orderAmount"));
            order.productAmount = new BigDecimal(node.elementTextTrim("productAmount"));
            order.orderDeliveryFee = new BigDecimal(node.elementTextTrim("orderDeliveryFee"));
            order.orderNeedInvoice = Integer.parseInt(node.elementTextTrim("orderNeedInvoice"));

            order.orderCreateTime = parseDate(node.elementTextTrim("orderCreateTime"));
            order.updateTime = parseDate(node.elementTextTrim("updateTime"));

            return order;
        }
    };

    // 订单详细信息解析器
    public static Parser<YihaodianOrder> fullParser = new Parser<YihaodianOrder>() {
        @Override
        public YihaodianOrder parse(Element node) {
            //解析订单基本信息
            Element baseInfoNode = node.element("orderDetail");
            if(baseInfoNode == null) {
                Logger.info("can not get node: orderDetail");
                return null;
            }

            YihaodianOrder order = new YihaodianOrder();
            order.orderId = Long.parseLong(baseInfoNode.elementTextTrim("orderId"));
            order.orderCode = baseInfoNode.elementTextTrim("orderCode");
            order.orderStatus = OrderStatus.valueOf(baseInfoNode.elementTextTrim("orderStatus"));
            order.orderAmount = new BigDecimal(baseInfoNode.elementTextTrim("orderAmount"));
            order.productAmount = new BigDecimal(baseInfoNode.elementTextTrim("productAmount"));
            order.orderDeliveryFee = new BigDecimal(baseInfoNode.elementTextTrim("orderDeliveryFee"));
            order.orderNeedInvoice = Integer.parseInt(node.elementTextTrim("orderNeedInvoice"));
            order.orderCreateTime = parseDate(node.elementTextTrim("orderCreateTime"));
            order.updateTime = parseDate(node.elementTextTrim("updateTime"));

            order.deliverySupplierId = Integer.parseInt(baseInfoNode.elementTextTrim("eliverySupplierId"));
            order.deliveryDate = parseDate(baseInfoNode.elementTextTrim("deliveryDate"));
            order.expressNbr = baseInfoNode.elementTextTrim("merchantExpressNbr");
            order.receiveDate = parseDate(baseInfoNode.elementTextTrim("receiveDate"));
            order.deliveryMethodType = Integer.parseInt(baseInfoNode.elementTextTrim("deliveryMethodType"));
            order.goodReceiverAddress = baseInfoNode.elementTextTrim("goodReceiverAddress");
            order.goodReceiverCity = baseInfoNode.elementTextTrim("goodReceiverCity");
            order.goodReceiverCountry = baseInfoNode.elementTextTrim("goodReceiverCounty");
            order.goodReceiverMobile = baseInfoNode.elementTextTrim("goodReceiverMoblie");
            order.goodReceiverName = baseInfoNode.elementTextTrim("goodReceiverName");
            order.goodReceiverPhone = baseInfoNode.elementTextTrim("goodReceiverPhone");
            order.goodReceiverPostcode = baseInfoNode.elementTextTrim("goodReceiverPostCode");
            order.goodReceiverProvince = baseInfoNode.elementTextTrim("goodReceiverProvince");
            order.fostRebate = Float.parseFloat(baseInfoNode.elementTextTrim("orderFostRebate"));
            order.promotionDiscount = new BigDecimal(baseInfoNode.elementTextTrim("orderPromotionDiscount"));
            order.payServiceType = Integer.parseInt(baseInfoNode.elementTextTrim("payServiceType"));
            order.recompensPoints = Float.parseFloat(baseInfoNode.elementTextTrim("recompensPoints"));

            order.createPayTime = parseDate(baseInfoNode.elementTextTrim("orderCreatePayTime"));
            order.paymentConfirmDate = parseDate(baseInfoNode.elementTextTrim("orderPaymentConfirmDate"));

            //解析订单条目信息
            Element orderItemsNode = node.element("orderItemList");
            if (orderItemsNode != null){
                for(Object o : orderItemsNode.elements()){
                    OrderItem orderItem = OrderItem.parser.parse((Element)o);
                    if(orderItem != null){
                        order.orderItems.add(orderItem);
                    }
                }
            }

            return order;
        }
    };

    private static Date parseDate(String date){
        if(date == null || "".equals(date.trim())){
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            return simpleDateFormat.parse(date);
        }catch (ParseException e){
            return null;
        }
    }
}
