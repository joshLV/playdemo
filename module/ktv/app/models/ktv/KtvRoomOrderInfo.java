package models.ktv;

import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * User: tanglq
 * Date: 13-4-10
 * Time: 上午11:57
 */
@Entity
@Table(name = "ktv_room_order_info")
public class KtvRoomOrderInfo extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    public Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    public OrderItems orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ktv_room_id", nullable = false)
    public KtvRoom ktvRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ktv_room_type_id", nullable = false)
    public KtvRoomType ktvRoomType;

    @Enumerated(EnumType.STRING)
    public KtvOrderStatus status;
    /**
     * 预定日期
     */
    @Column(name = "scheduled_day")
    public Date scheduledDay;

    /**
     * 预定时间
     */
    @Column(name = "scheduled_time")
    public String scheduledTime;

    @Column(name = "created_at")
    public Date createdAt;

    public KtvRoomOrderInfo(Goods goods, OrderItems orderItem, KtvRoom ktvRoom, KtvRoomType ktvRoomType, Date scheduledDay, String scheduledTime) {
        this.goods = goods;
        this.orderItem = orderItem;
        this.ktvRoom = ktvRoom;
        this.ktvRoomType = ktvRoomType;
        this.scheduledDay = scheduledDay;
        this.scheduledTime = scheduledTime;
        this.status = KtvOrderStatus.LOCK;
        this.createdAt = new Date();

    }

    /**
     * 取消10分钟前未付款的订单
     */
    public void cancelKtvRoom() {
        if (this.status == KtvOrderStatus.LOCK) {
            this.status = KtvOrderStatus.CANCELED;
            this.orderItem.status = OrderStatus.CANCELED;
            this.orderItem.save();
            this.save();
        }
    }
}
