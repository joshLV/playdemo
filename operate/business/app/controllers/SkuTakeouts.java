package controllers;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.RealGoodsReturnEntry;
import models.order.TakeoutItem;
import models.sales.Goods;
import models.sales.InventoryStock;
import models.sales.OrderBatch;
import models.sales.Sku;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 货品出库.
 * <p/>
 * User: sujie
 * Date: 3/8/13
 * Time: 11:27 AM
 */
@With(OperateRbac.class)
@ActiveNavigation("sku_takeouts_index")
public class SkuTakeouts extends Controller {
    /**
     * 显示出库汇总信息
     */
    public static void index() {
        //先检查是否有退货订单，如果有待处理的退货订单必须先处理掉再做出库操作。以确保库存的准确的情况下正确出库。
        long returnEntryCount = RealGoodsReturnEntry.countHandling(Supplier.getShihui().id);

        final Date toDate = new Date();

        Boolean hasHandleTakeOutsPermission = ContextedPermission.hasPermission("INVENTORY");

        //1 统计总的待出库货品及数量
        Map<Sku, Long> preparingTakeoutSkuMap = OrderItems.findTakeout(toDate);
        //2 获取无法出库订单项
        List<OrderItems> deficientOrderItemList = InventoryStock.getDeficientOrderItemList(preparingTakeoutSkuMap, toDate);
        //3 无法出库订单
        List<Order> deficientOrderList = InventoryStock.getOrderListByItem(deficientOrderItemList);
        //4 统计应该出库货品及数量
        Map<Sku, Long> takeoutSkuMap = InventoryStock.statisticOutCount(preparingTakeoutSkuMap, deficientOrderList);
        //5 获取待出库订单
        List<Order> allPaidOrders = OrderItems.findPaidRealGoodsOrders(toDate);
        //6 获取待出库订单数
        long paidOrderCount = allPaidOrders.size();
        //7 可出库订单
        List<Order> stockoutOrderList = OrderItems.getStockOutOrders(allPaidOrders, deficientOrderList);

        //8 获取可出库订单计算出的货品平均售价
        Map<Sku, BigDecimal> skuAveragePriceMap = OrderItems.getSkuAveragePriceMap(stockoutOrderList, takeoutSkuMap);

        render(paidOrderCount, returnEntryCount, preparingTakeoutSkuMap, takeoutSkuMap, skuAveragePriceMap, stockoutOrderList, deficientOrderList, toDate, hasHandleTakeOutsPermission);
    }

    /**
     * 出库
     */
    @Right("INVENTORY")
    public static void stockOut(@As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss.SSS"}) Date toDate) {
        String operatorName = OperateRbac.currentUser().userName;
        //1 统计总的待出库货品及数量
        Map<Sku, Long> preparingTakeoutSkuMap = OrderItems.findTakeout(toDate);

        //2 获取无法出库订单项
        List<OrderItems> deficientOrderItemList = InventoryStock.getDeficientOrderItemList(preparingTakeoutSkuMap, toDate);
        //3 无法出库订单
        List<Order> deficientOrderList = InventoryStock.getOrderListByItem(deficientOrderItemList);
        //4 统计应该出库货品及数量
        Map<Sku, Long> takeoutSkuMap = InventoryStock.statisticOutCount(preparingTakeoutSkuMap, deficientOrderList);
        //5 获取待出库订单
        List<Order> allPaidOrders = OrderItems.findPaidRealGoodsOrders(toDate);
        //6 可出库订单
        List<Order> stockoutOrderList = OrderItems.getStockOutOrders(allPaidOrders, deficientOrderList);

        //7 标记出库订单的状态为待打包状态
        if (stockoutOrderList.size() == 0) {
            Validation.addError("stockoutOrderId", "validation.noStockoutOrder");
            render("SkuTakeouts/result.html");
        }
        for (Order dbOrder : stockoutOrderList) {
            final Goods noSkuGoods = OrderItems.findNoSkuGoods(dbOrder.id);
            if (dbOrder.containsRealGoods() && noSkuGoods != null) {
                Validation.addError("stockoutOrderId", "validation.noSku", noSkuGoods.shortName);
                render("SkuTakeouts/result.html");
            }
        }
        //8 创建总出库单
        InventoryStock stock = InventoryStock.createInventoryStock(Supplier.getShihui(), operatorName);

        //9 创建出库单对应的批次
        OrderBatch orderBatch = new OrderBatch(Supplier.getShihui(), operatorName, Long.parseLong(String.valueOf(stockoutOrderList.size())));
        orderBatch.stock = stock;
        orderBatch.save();

        for (Order dbOrder : stockoutOrderList) {
            dbOrder.status = OrderStatus.PREPARED;
            dbOrder.save();
            for (OrderItems orderItem : dbOrder.orderItems) {
                orderItem.status = OrderStatus.PREPARED;
                orderItem.orderBatch = orderBatch;
                TakeoutItem takeoutItem = new TakeoutItem(orderItem, orderItem.goods.sku, orderItem.buyNumber * orderItem.goods.skuCount);
                takeoutItem.save();
                orderItem.takeOutItems.add(takeoutItem);
                orderItem.save();
            }

        }

        //10 获取可出库订单计算出的货品平均售价
        Map<Sku, BigDecimal> skuAveragePriceMap = OrderItems.getSkuAveragePriceMap(stockoutOrderList, takeoutSkuMap);

        stock.remark = "发货单批次:" + orderBatch.id;
        stock.save();

        //11 按商品创建出库单明细
        for (Sku sku : takeoutSkuMap.keySet()) {
            //创建出库详单信息
            InventoryStock.createInventoryStockItem(sku, 0L - takeoutSkuMap.get(sku), stock, skuAveragePriceMap.get(sku));

            //修改入库的剩余库存
            InventoryStock.updateInventoryStockRemainCount(sku, takeoutSkuMap.get(sku));
        }
        render("SkuTakeouts/result.html");
    }
}
