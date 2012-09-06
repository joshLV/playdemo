package models.yihaodian;

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
    public YihaodianOrder order;

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
    public Long outerId;

    public static Parser<OrderItem> parser = new Parser<OrderItem>() {
        @Override
        public OrderItem parse(Element node) {
            OrderItem orderItem = new OrderItem();
            orderItem.orderItemId = Long.parseLong(node.elementTextTrim("id"));
            orderItem.productCName = node.elementTextTrim("productCName");
            orderItem.orderItemAmount = new BigDecimal(node.elementTextTrim("orderItemAmount"));
            orderItem.orderItemNum = Integer.parseInt(node.elementTextTrim("orderItemNum"));
            orderItem.orderItemPrice = new BigDecimal(node.elementTextTrim("orderItemPrice"));
            orderItem.originalPrice = new BigDecimal(node.elementTextTrim("originalPrice"));
            orderItem.taxRate = new BigDecimal(node.elementTextTrim("taxRate"));
            orderItem.productId = Long.parseLong(node.elementTextTrim("productId"));
            orderItem.itemLeaf = Integer.parseInt(node.elementTextTrim("isItemLeaf"));
            orderItem.merchantId = Long.parseLong(node.elementTextTrim("merchantId"));
            orderItem.promoteType = Integer.parseInt(node.elementTextTrim("promoteType"));
            orderItem.outerId = Long.parseLong(node.elementTextTrim("outerId"));

            try{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if(node.elementText("processFinishDate") != null){
                    orderItem.processFinishDate = simpleDateFormat.parse(node.elementTextTrim("processFinishDate"));
                }
                orderItem.updateTime = simpleDateFormat.parse(node.elementTextTrim("updateTime"));
            }catch (ParseException e){/* */}

            return orderItem;
        }
    };
}
