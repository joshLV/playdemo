package controllers;

import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.InventoryStock;
import models.sales.Sku;
import play.mvc.Controller;

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
public class SkuTakeouts extends Controller {


    /**
     * 显示出库汇总信息
     */
    public static void index() {
        //获取出库单
        final Date toDate = new Date();
        //统计总的待出库货品及数量
        Map<Sku, Long> takeoutSkuMap = OrderItems.findTakeout(toDate);
        //统计实际出库货品及数量
        Map<Sku, List<Order>> deficientOrderMap = InventoryStock.getDeficientOrders(takeoutSkuMap, toDate);
        Map<Sku, Long> deficientSkuMap = InventoryStock.statisticOutCount(takeoutSkuMap, deficientOrderMap);

        //待出库订单数
        long paidOrderCount = OrderItems.countPaidOrders(toDate);
        List<Order> allPaidOrders = OrderItems.findPaidOrders(toDate);
        //无法出库订单
        List<Order> deficientOrderList = InventoryStock.getDeficientOrderList(toDate);
        //可出库订单
        List<Order> stockoutOrderList = OrderItems.getStockOutOrders(allPaidOrders, deficientOrderList);

        //无法出库的订单数
        long deficientOrderCount = stockoutOrderList.size();
        render(paidOrderCount, deficientOrderCount, takeoutSkuMap, deficientSkuMap, deficientOrderList, stockoutOrderList, toDate);
    }

    /**
     * 出库
     */
    public static void stockOut(Date toDate, Order... stockoutOrderList) {
        //统计总的待出库货品及数量
        Map<Sku, Long> takeoutSkuMap = OrderItems.findTakeout(toDate);
        //统计实际出库货品及数量
        Map<Sku, List<Order>> deficientOrderMap = InventoryStock.getDeficientOrders(takeoutSkuMap, toDate);
        Map<Sku, Long> deficientSkuMap = InventoryStock.statisticOutCount(takeoutSkuMap, deficientOrderMap);
        for (Sku sku : takeoutSkuMap.keySet()) {
            Long deficientCount = deficientSkuMap.get(sku);
            if (deficientCount == null) {
                //创建出库单
                InventoryStock.createInventoryStock(sku, takeoutSkuMap.get(sku));
                //修改入库的剩余库存
                InventoryStock.updateInventoryStockRemainCount(sku, takeoutSkuMap.get(sku));
            }
        }

        //标记出库订单的状态为代打包状态
        for (Order order : stockoutOrderList) {
            order.status = OrderStatus.PREPARED;
            order.save();
            for (OrderItems orderItem : order.orderItems) {
                orderItem.status = OrderStatus.PREPARED;
                orderItem.save();
            }
        }
        render();
    }
}