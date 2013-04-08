package models.order;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * 实物商品退货单.
 * <p/>
 * User: sujie
 * Date: 3/22/13
 * Time: 11:11 AM
 */
@Entity
@Table(name = "real_goods_return_entry")
public class RealGoodsReturnEntry extends Model {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    public OrderItems orderItems;
    /**
     * 退货数量
     */
    @Required
    @Column(name = "returned_count")
    public Long returnedCount;

    /**
     * 退货原因
     */
    @Required
    public String reason;

    /**
     * 未收到货原因.
     */
    @Column(name = "unreceived_reason")
    public String unreceivedReason;

    /**
     * 实物退货状态
     */
    @Enumerated(EnumType.STRING)
    public RealGoodsReturnStatus status;

    /**
     * 退货单创建时间, 申请时间.
     */
    @Column(name = "created_at")
    public Date createdAt = new Date();

    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 实物退回时间.
     */
    @Column(name = "returned_at")
    public Date returnedAt;

    /**
     * 退货.
     */
    @Column(name = "returned_by")
    public String returnedBy;

    /**
     * 根据订单项查找退货状态.
     *
     * @param orderItems 订单项
     * @return 退货状态
     */
    public static RealGoodsReturnStatus getReturnStatus(OrderItems orderItems) {
        return find("select status from RealGoodsReturnEntry where orderItems=?", orderItems).first();
    }

    /**
     * 根据订单项ID查找退货单.
     *
     * @param orderItemId 订单项
     * @return 退货单
     */
    public static RealGoodsReturnEntry findByOrderItem(Long orderItemId) {
        OrderItems orderItems = new OrderItems();
        orderItems.id = orderItemId;
        return find("orderItems=?", orderItems).first();
    }

    /**
     * 查询指定商户处理中的退货单数量.
     *
     * @param supplierId   商户标识
     * @return  退货单数量
     */
    public static long countHandling(Long supplierId) {
        Long count = count("status=? and orderItems.goods.supplierId=?", RealGoodsReturnStatus.RETURNING, supplierId);
        return count == null ? 0L : count;
    }

    /**
     * 查询指定商户处理中的退货单数量.
     *
     * @return  退货单数量
     */
    public static RealGoodsReturnEntry findHandling(String orderNumber, String goodsCode) {
        return find("status=? and orderItems.order.orderNumber=? and orderItems.goods.code=?", RealGoodsReturnStatus.RETURNING,orderNumber,goodsCode).first();
    }

    /**
     * 查询指定商户处理中的退货单列表.
     *
     * @param condition
     * @return  退货单列表
     */
    public static JPAExtPaginator<RealGoodsReturnEntry> getPage(RealGoodsReturnEntryCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<RealGoodsReturnEntry> page = new JPAExtPaginator<>("RealGoodsReturnEntry r","r",RealGoodsReturnEntry.class, condition.getFilter(),
                condition.getParams()).orderBy("r.createdAt desc");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setBoundaryControlsEnabled(false);
        return page;
    }
}
