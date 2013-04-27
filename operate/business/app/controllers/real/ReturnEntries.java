package controllers.real;

import controllers.OperateRbac;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.RealGoodsReturnEntry;
import models.order.RealGoodsReturnEntryCondition;
import models.order.RealGoodsReturnStatus;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.StockActionType;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * 退货单管理.
 * <p/>
 * User: sujie
 * Date: 3/25/13
 * Time: 10:18 AM
 */

@With(OperateRbac.class)
public class ReturnEntries extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 显示指定的退货单信息.
     *
     * @param orderItemId   订单项ID
    public static void show(Long orderItemId) {
    RealGoodsReturnEntry returnEntry = RealGoodsReturnEntry.findByOrderItem(orderItemId);
    render(returnEntry);
    }
     */

    /**
     * 查看待处理的视惠的实物退货单.
     */
    @ActiveNavigation("return_entries_index")
    public static void index(RealGoodsReturnEntryCondition condition) {
        Boolean hasHandleReturnGoodsPermission = ContextedPermission.hasPermission("INVENTORY");
        if (condition == null) {
            condition = new RealGoodsReturnEntryCondition(Supplier.getShihui().id, RealGoodsReturnStatus.RETURNING);
        }
        condition.supplierId = Supplier.getShihui().id;
        final int page = getPage();
        JPAExtPaginator<RealGoodsReturnEntry> entryPage = RealGoodsReturnEntry.getPage(condition, page, PAGE_SIZE);
        render(entryPage, condition, hasHandleReturnGoodsPermission);
    }

    /**
     * 仓库管理员确认收到货.
     *
     * @param id
     */
    @ActiveNavigation("return_entries_index")
    @Right("INVENTORY")
    public static void received(Long id) {
        //1、修改退货单状态.
        RealGoodsReturnEntry entry = RealGoodsReturnEntry.findById(id);
        entry.status = RealGoodsReturnStatus.RETURNED;
        entry.returnedAt = new Date();
        entry.returnedBy = OperateRbac.currentUser().userName;
        entry.save();
        //2、产生入库单.
        InventoryStock inventoryStock = new InventoryStock();
        inventoryStock.supplier = Supplier.getShihui();
        inventoryStock.actionType = StockActionType.RETURN;
        inventoryStock.createdBy = OperateRbac.currentUser().userName;
        inventoryStock.storekeeper = OperateRbac.currentUser().userName;
        inventoryStock.create();
        InventoryStockItem stockItem = new InventoryStockItem(inventoryStock);
        //Todo: stock -> item  1:n  orderitems --> takeoutitem 1:n   used by  for (TakeoutItem i : entry.orderItems.takeOutItems) {}
//        GoodsHistory goodsHistory =GoodsHistory.findById(entry.orderItems.goodsHistoryId);
//        Goods temporalGoods = Goods.findById(goodsHistory.goodsId);
//        stockItem.sku = temporalGoods.sku;
//        stockItem.changeCount = entry.returnedCount * temporalGoods.skuCount;
//        stockItem.remainCount = stockItem.changeCount;
        stockItem.sku = entry.orderItems.takeOutItems.get(0).sku;
        stockItem.changeCount = entry.orderItems.takeOutItems.get(0).count;
        stockItem.remainCount = stockItem.changeCount;

        stockItem.effectiveAt = entry.orderItems.goods.effectiveAt;
        stockItem.expireAt = entry.orderItems.goods.expireAt;
        stockItem.price = entry.orderItems.goods.originalPrice;
        stockItem.create();
        //3、退款
        OrderItems.handleRefund(entry.orderItems, entry.returnedCount);

        index(null);

    }

    /**
     * 仓库管理员确认未收到货.
     *
     * @param id
     */

    @ActiveNavigation("return_entries_index")
    @Right("INVENTORY")
    public static void unreceived(Long id, String unreceivedReason) {
        //1、修改退货单状态
        RealGoodsReturnEntry entry = RealGoodsReturnEntry.findById(id);
        entry.status = RealGoodsReturnStatus.RETURNED;
        entry.returnedAt = new Date();
        entry.returnedBy = OperateRbac.currentUser().userName;
        //2、填写退货单未收到货原因.
        entry.unreceivedReason = unreceivedReason;
        entry.save();
        //3、退款
        OrderItems.handleRefund(entry.orderItems, entry.returnedCount);

        index(null);
    }

    /**
     * 客服在订单管理的详单处点击退货的处理.
     * <p/>
     * 如果是实物订单，并且订单项状态为已付款，则表示未出库，直接进行退款处理
     * 如果是实物订单，但订单项状态为待打包、已发货、已上传，则表示已经出库，则进行申请退款处理
     *
     * @param entry
     */
    @Right("RETURN_GOODS")
    public static void returnGoods(RealGoodsReturnEntry entry) {
        System.out.println("entry.unreceivedReason:" + entry.unreceivedReason);

        Long orderId = null;
        if (entry.orderItems != null && entry.orderItems.id != null && entry.orderItems.id != 0L) {
            OrderItems orderItems = OrderItems.findById(entry.orderItems.id);
            Logger.info("orderItems.status=" + orderItems.status + ", id=" + entry.orderItems.id);
            orderId = orderItems.order.id;
            switch (orderItems.status) {
                case PAID:
                    //todo 退款处理，有待测试
                    Logger.info("do PAID status");
                    String result = OrderItems.handleRefund(orderItems, entry.returnedCount);

                    if (!result.equals("")) {
                        renderJSON(result);
                    }

                    entry.status = RealGoodsReturnStatus.RETURNED;
                    break;
                case PREPARED:
                case UPLOADED:
                case SENT:
                    entry.status = RealGoodsReturnStatus.RETURNING;
                    entry.orderItems.status = OrderStatus.RETURNING;
                    entry.orderItems.save();
                    break;
            }
        }

        entry.createdBy = OperateRbac.currentUser().userName;
        entry.create();

        renderJSON(orderId);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

}
