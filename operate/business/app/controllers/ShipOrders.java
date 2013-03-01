package controllers;

import models.order.SupplierShipOrder;
import play.mvc.Controller;

import java.util.List;

/**
 * 发货单管理.
 * <p/>
 * User: sujie
 * Date: 2/27/13
 * Time: 2:17 PM
 */
public class ShipOrders extends Controller {

    /**
     * 显示发货单批次列表.
     */
    public static void index() {
//        List<Supplier> supplierList = Supplier.findUnDeleted();
        //todo
        List<SupplierShipOrder> shipOrderList = SupplierShipOrder.getTop(50);
        render(shipOrderList);
    }

    /**
     * 导出发货单.
     */
    public static void exportDispatchList() {
        //todo
    }
}