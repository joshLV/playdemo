package models.ktv;

import models.order.OrderItems;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * User: tanglq
 * Date: 13-4-10
 * Time: 上午11:57
 */
@Entity
@Table(name="ktv_room_order_info")
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
}
