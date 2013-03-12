package controllers;

import controllers.supplier.SupplierInjector;
import models.order.OrderShippingInfo;
import models.order.OrderItems;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-11
 * Time: 下午3:49
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierLogistics extends Controller {
    public static void index() {
        List<OrderItems> orderItemsList = OrderItems.find("goods.supplierId=? and orderBatch is not null", SupplierRbac.currentUser().supplier.id).fetch();
        render(orderItemsList);
    }

    /**
     * 导出发货单
     */
    public static void exportLogistics() {
        List<OrderItems> orderItemsList = OrderItems.find("goods.supplierId=? and orderBatch is not null", SupplierRbac.currentUser().supplier.id).fetch();
        List<OrderShippingInfo> orderShippingInfoList = new ArrayList<>();
        for (OrderItems orderItem : orderItemsList) {
            OrderShippingInfo orderShippingInfo = OrderShippingInfo.find("orderItems=?", orderItem).first();
            orderShippingInfoList.add(orderShippingInfo);
        }

        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "发货单导出_" + System.currentTimeMillis() + ".xls");
        render(orderShippingInfoList);
    }

}
