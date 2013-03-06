package models.sales;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import models.order.OrderItems;

/**
 * 消费者的发货单.
 * <p/>
 * User: sujie
 * Date: 3/5/13
 * Time: 4:02 PM
 */
@Entity
@Table(name = "consumer_ship_order")
public class ConsumerShipOrder extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = true)
    public OrderItems orderItems;

    /**
     * 物流公司
     */
    @Column(name = "logistics_company")
    public String logisticsCompany;

    /**
     * 物流单号
     */
    @Column(name = "tracking_number")
    public String trackingNumber;

    /**
     * 对应的出库单
     */
    @Column(name = "inventory_stock")
    public InventoryStock inventoryStock;
}
