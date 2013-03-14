package controllers;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.sales.InventoryStock;
import models.sales.OrderBatch;
import models.sales.Sku;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
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
        final Date toDate = new Date();
        //1 统计总的待出库货品及数量
        Map<Sku, Long> preparingTakeoutSkuMap = OrderItems.findTakeout(toDate);
        //2 获取无法出库订单项
        List<OrderItems> deficientOrderItemList = InventoryStock.getDeficientOrderItemList(toDate);
        //3 统计缺货订单
        Map<Sku, List<Order>> deficientOrderMap = InventoryStock.getDeficientOrders(deficientOrderItemList);
        //4 无法出库订单
        List<Order> deficientOrderList = InventoryStock.getOrderListByItem(deficientOrderItemList);
        //5 统计应该出库货品及数量
        Map<Sku, Long> takeoutSkuMap = InventoryStock.statisticOutCount(preparingTakeoutSkuMap, deficientOrderList);
        //6 获取待出库订单
        List<Order> allPaidOrders = OrderItems.findPaidRealGoodsOrders(toDate);
        for (Order allPaidOrder : allPaidOrders) {
            System.out.println("allPaidOrder.id:" + allPaidOrder.id);
        }
        //7 获取待出库订单数
        long paidOrderCount = allPaidOrders.size();
        //8 可出库订单
        List<Order> stockoutOrderList = OrderItems.getStockOutOrders(allPaidOrders, deficientOrderList);
        //9 获取可出库订单计算出的货品平均售价
        Map<Sku, BigDecimal> skuAveragePriceMap = OrderItems.getSkuAveragePriceMap(stockoutOrderList, takeoutSkuMap);

        render(paidOrderCount, takeoutSkuMap, skuAveragePriceMap, stockoutOrderList, deficientOrderList, toDate);
    }

    /**
     * 出库
     */
    public static void stockOut(Date toDate, List<Long> stockoutOrderId) {
        String operatorName = OperateRbac.currentUser().userName;
        //1 统计总的待出库货品及数量
        Map<Sku, Long> preparingTakeoutSkuMap = OrderItems.findTakeout(toDate);
        //2 获取无法出库订单项
        List<OrderItems> deficientOrderItemList = InventoryStock.getDeficientOrderItemList(toDate);
        //3 统计缺货订单
        Map<Sku, List<Order>> deficientOrderMap = InventoryStock.getDeficientOrders(deficientOrderItemList);
        //4 无法出库订单
        List<Order> deficientOrderList = InventoryStock.getOrderListByItem(deficientOrderItemList);
        //5 统计应该出库货品及数量
        Map<Sku, Long> takeoutSkuMap = InventoryStock.statisticOutCount(preparingTakeoutSkuMap, deficientOrderList);
        //6 获取待出库订单
        List<Order> allPaidOrders = OrderItems.findPaidRealGoodsOrders(toDate);
        //7 可出库订单
        List<Order> stockoutOrderList = OrderItems.getStockOutOrders(allPaidOrders, deficientOrderList);

        //8 标记出库订单的状态为待打包状态
        for (Order dbOrder : stockoutOrderList) {
            final Goods noSkuGoods = OrderItems.findNoSkuGoods(dbOrder.id);
            if (dbOrder.containsRealGoods() && noSkuGoods != null) {
                Validation.addError("stockoutOrderId", "validation.noSku", noSkuGoods.shortName);
                render("SkuTakeouts/result.html");
            }
        }
        //9 创建总出库单
        InventoryStock stock = InventoryStock.createInventoryStock(Supplier.getShihui(), operatorName);

        //10 创建出库单对应的批次
        OrderBatch orderBatch = new OrderBatch(Supplier.getShihui(), operatorName);
        orderBatch.stock = stock;
        orderBatch.save();

        for (Order dbOrder : stockoutOrderList) {
            dbOrder.status = OrderStatus.PREPARED;
            dbOrder.save();
            for (OrderItems orderItem : dbOrder.orderItems) {
                orderItem.status = OrderStatus.PREPARED;
                orderItem.orderBatch = orderBatch;
                orderItem.save();
            }
        }

        //11 获取可出库订单计算出的货品平均售价
        Map<Sku, BigDecimal> skuAveragePriceMap = OrderItems.getSkuAveragePriceMap(stockoutOrderList, takeoutSkuMap);

        //12 按商品创建出库单明细
        for (Sku sku : takeoutSkuMap.keySet()) {
            //创建出库详单信息
            InventoryStock.createInventoryStockItem(sku, takeoutSkuMap.get(sku), stock, skuAveragePriceMap.get(sku));
            //修改入库的剩余库存
            InventoryStock.updateInventoryStockRemainCount(sku, takeoutSkuMap.get(sku));
        }
        render("SkuTakeouts/result.html");
    }
}
