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
import play.Play;
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
//        condition.supplierId = Supplier.getShihui().id;
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
    public static void received(Long id, Long stockInCount) {
        //1、修改退货单状态.
        RealGoodsReturnEntry entry = RealGoodsReturnEntry.findById(id);
        OrderItems orderItems = entry.orderItems;
        Supplier supplier = Supplier.findById(orderItems.goods.supplierId);
        //只有视惠发货的才有入库
        if (supplier.equals(Supplier.getShihui())) {
            doInstock(orderItems, entry);
//            entry.stockInCount = stockInCount;
//            entry.status = RealGoodsReturnStatus.RETURNED;
//            entry.returnedAt = new Date();
//            entry.returnedBy = OperateRbac.currentUser().userName;
//            entry.save();
//            //2、产生入库单.
//            InventoryStock inventoryStock = new InventoryStock();
//            inventoryStock.supplier = Supplier.getShihui();
//            inventoryStock.actionType = StockActionType.RETURN;
//            inventoryStock.createdBy = OperateRbac.currentUser().userName;
//            inventoryStock.storekeeper = OperateRbac.currentUser().userName;
//            inventoryStock.create();
//            InventoryStockItem stockItem = new InventoryStockItem(inventoryStock);
//            stockItem.sku = entry.orderItems.takeOutItems.get(0).sku;
//            stockItem.changeCount = stockInCount;
//            stockItem.remainCount = stockItem.changeCount;
//
//            stockItem.effectiveAt = entry.orderItems.goods.effectiveAt;
//            stockItem.expireAt = entry.orderItems.goods.expireAt;
//            stockItem.price = entry.orderItems.goods.originalPrice;
//            stockItem.create();
            //3、退款
            //按数量退款
            if (entry.partialRefundPrice == null && entry.returnedCount > 0) {
                OrderItems.handleRealGoodsRefund(entry.orderItems, entry.returnedCount);
            }  //部分退款
            else if (entry.partialRefundPrice != null && entry.returnedCount == 0) {
                OrderItems.handleRealGoodsRefundByPartialRefundPrice(entry.orderItems, entry.partialRefundPrice);
            }
        } else {
            entry.status = RealGoodsReturnStatus.RETURNED;
            entry.returnedAt = new Date();
            entry.returnedBy = OperateRbac.currentUser().userName;
            entry.save();
            //第三方发货 退款
            //按数量退款
            if (entry.partialRefundPrice == null && entry.returnedCount > 0) {
                OrderItems.handleRealGoodsRefund(entry.orderItems, entry.returnedCount);
            }  //部分退款
            else if (entry.partialRefundPrice != null && entry.returnedCount == 0) {
                OrderItems.handleRealGoodsRefundByPartialRefundPrice(entry.orderItems, entry.partialRefundPrice);
            }
        }
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
        //TODO 3、暂时不退款，如需退款，线下操作
//        OrderItems.handleRealGoodsRefund(entry.orderItems, entry.returnedCount);
        //部分退款
        //按数量退款
        if (entry.partialRefundPrice == null && entry.returnedCount > 0) {
            OrderItems.handleRealGoodsRefund(entry.orderItems, entry.returnedCount);
        }  //部分退款
        else if (entry.partialRefundPrice != null && entry.returnedCount == 0) {
            OrderItems.handleRealGoodsRefundByPartialRefundPrice(entry.orderItems, entry.partialRefundPrice);
        }
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

        Long orderId = null;
        if (entry.orderItems != null && entry.orderItems.id != null && entry.orderItems.id != 0L) {
            OrderItems orderItems = OrderItems.findById(entry.orderItems.id);
            Logger.info("orderItems.status=" + orderItems.status + ", id=" + entry.orderItems.id);
            orderId = orderItems.order.id;
            Supplier supplier = Supplier.findById(orderItems.goods.supplierId);
            String result = "";
            switch (orderItems.status) {
                case PAID:
                    Logger.info("do PAID status");
                    //付款场合只要退款给分销平台
                    result = OrderItems.handleRealGoodsOfNoSendRefund(orderItems, entry.returnedCount);

                    if (!result.equals("")) {
                        renderJSON(result);
                    }

                    entry.status = RealGoodsReturnStatus.RETURNED;
                    entry.returnedAt = new Date();
                    entry.returnedBy = OperateRbac.currentUser().userName;
                    entry.save();
                    //只有视惠发货的才有入库
                    if (supplier.equals(Supplier.getShihui())) {
                        doInstock(orderItems, entry);
                    }

                    break;
                case PREPARED:
                    //待打包（未发货）的场合只要退款给分销平台
                    if (orderItems.shippingInfo != null && StringUtils.isBlank(orderItems.shippingInfo.expressNumber)) {
                        OrderItems.handleRealGoodsOfNoSendRefund(orderItems, entry.returnedCount);
                        entry.returnedAt = new Date();
                        entry.returnedBy = OperateRbac.currentUser().userName;
                        entry.status = RealGoodsReturnStatus.RETURNED;
                        //只有视惠发货的才有入库
                        if (supplier.equals(Supplier.getShihui())) {
                            doInstock(orderItems, entry);
                        }
                    } else {
                        entry.status = RealGoodsReturnStatus.RETURNING;
                        entry.orderItems.status = OrderStatus.RETURNING;
                        entry.orderItems.save();
                    }
                    break;
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

    /**
     * 视惠退款场合入库
     *
     * @param entry
     */
    private static void doInstock(OrderItems orderItems, RealGoodsReturnEntry entry) {
        if (orderItems.orderBatch ==null){
            return;
        }
        InventoryStockItem preStockItem = InventoryStockItem.find("stock = ? and sku=?",orderItems.orderBatch.stock,orderItems.goods.sku).first();
        if (preStockItem==null){
            return;

        }
        entry.stockInCount = preStockItem.changeCount;
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
        if (entry.orderItems.takeOutItems.size() > 0) {
            stockItem.sku = entry.orderItems.takeOutItems.get(0).sku;
        }
        stockItem.changeCount = -(preStockItem.changeCount);
        stockItem.remainCount = stockItem.changeCount;

        stockItem.effectiveAt = entry.orderItems.goods.effectiveAt;
        stockItem.expireAt = entry.orderItems.goods.expireAt;
        stockItem.price = preStockItem.price;
        stockItem.create();
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

}
