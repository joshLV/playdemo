package models.job.yihaodian;

import org.dom4j.Element;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-8-30
 */
@Entity
@Table(name = "yihaodian_order")
public class Order extends Model{
    @Column(name = "order_id")
    public Long orderId;                //订单编号

    @Column(name = "order_code")
    public String orderCode;            //订单编码

    @Column(name = "status")
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<OrderItem> orderItems;

    //---------- 补充信息 -----
    /*
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

    @Column(name = "order_fost_rebate")
    public String orderFostRebate;
    */


    // 订单摘要解析器
    public static Parser<Order> parser = new Parser<Order>() {
        @Override
        public Order parse(Element node) {
            Order order = new Order();
            order.orderId = Long.parseLong(node.elementText("orderId"));
            order.orderCode = node.elementText("orderCode");
            order.orderStatus = OrderStatus.getStatus(node.elementText("orderStatus"));
            order.orderAmount = new BigDecimal(node.elementText("orderAmount"));
            order.productAmount = new BigDecimal(node.elementText("productAmount"));
            order.orderDeliveryFee = new BigDecimal(node.elementText("orderDeliveryFee"));
            order.orderNeedInvoice = Integer.parseInt(node.elementText("orderNeedInvoice"));
            try{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                order.orderCreateTime = simpleDateFormat.parse(node.elementText("orderCreateTime"));
                order.updateTime = simpleDateFormat.parse(node.elementText("updateTime"));
            }catch (ParseException e){/* pass */ }

            return order;
        }
    };

    // 订单详细信息解析器
    public static Parser<Order> fullParser = new Parser<Order>() {
        @Override
        public Order parse(Element node) {
            //解析订单基本信息
            Element baseInfoNode = node.element("orderDetail");
            if(baseInfoNode == null) {
                return null;
            }
            Order order = parse(baseInfoNode);

            //解析订单条目信息
            Element orderItemsNode = node.element("orderItemList");
            if (orderItemsNode == null){
                return null;
            }
            for(Object o : orderItemsNode.elements()){
                OrderItem orderItem = OrderItem.parser.parse((Element)o);
                order.orderItems.add(orderItem);
            }
            return order;
        }
    };
}
