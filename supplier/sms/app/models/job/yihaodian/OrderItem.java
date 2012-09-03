package models.job.yihaodian;

import org.dom4j.Element;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author likang
 *
 * http://openapi.yihaodian.com/forward/inshop/orderItemObject.html
 *
 * 一号店订单明细条目
 * Date: 12-8-31
 */
@Entity
@Table(name = "yihaodian_order_item")
public class OrderItem extends Model {
    @Column(name = "order_item_id")
    public Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @Column(name = "product_c_name")
    public String productCName; //铲平名称

    @Column(name = "order_item_amount")
    public BigDecimal orderItemAmount;

    @Column(name = "order_item_num")
    public Integer orderItemNum;

    @Column(name = "order_item_price")
    public BigDecimal orderItemPrice;

    @Column(name = "original_price")
    public BigDecimal originalPrice;

    @Column(name = "tax_rate")
    public BigDecimal taxRate;

    @Column(name = "product_id")
    public Long productId;

    @Column(name = "item_leaf")
    public Integer itemLeaf;

    @Column(name = "merchantId")
    public Long merchantId;

    @Column(name = "promote_type")
    public Integer promoteType;

    @Column(name = "process_finish_date")
    public Date processFinishDate;

    @Column(name = "update_time")
    public Date updateTime;

    @Column(name = "outer_id")
    public String outerId;



    public static Parser<OrderItem> parser = new Parser<OrderItem>() {
        @Override
        public OrderItem parse(Element node) {
            OrderItem orderItem = new OrderItem();
            orderItem.orderItemId = Long.parseLong(node.elementText("id"));
//            orderItem.order = Order.find("byOrderId", node.elementText("orderId")).first();
            orderItem.productCName = node.elementText("productCName");
            orderItem.orderItemAmount = new BigDecimal(node.elementText("orderItemAmount"));
            orderItem.orderItemNum = Integer.parseInt(node.elementText("orderItemNum"));
            orderItem.orderItemPrice = new BigDecimal(node.elementText("orderItemPrice"));
            orderItem.originalPrice = new BigDecimal(node.elementText("originalPrice"));
            orderItem.taxRate = new BigDecimal(node.elementText("taxRate"));
            orderItem.productId = Long.parseLong(node.elementText("productId"));
            orderItem.itemLeaf = Integer.parseInt(node.elementText("isItemLeaf"));
            orderItem.merchantId = Long.parseLong(node.elementText("merchantId"));
            orderItem.promoteType = Integer.parseInt(node.elementText("promoteType"));
            orderItem.outerId = node.elementText("outerId");

            try{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                orderItem.processFinishDate = simpleDateFormat.parse(node.elementText("processFinishDate"));
                orderItem.updateTime = simpleDateFormat.parse(node.elementText("updateTime"));
            }catch (ParseException e){/* */}

            return orderItem;
        }
    };
}
