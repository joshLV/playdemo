package controllers;

import models.order.OrderItems;
import models.order.OrdersCondition;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

@With(SupplierRbac.class)
@ActiveNavigation("order_index")
/**
 * 商户不需要关注用户订单，因此此功能应取消！！！
 */
public class SupplierOrders extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 商户订单信息一览
     *
     * @param condition 查询条件
     */
    public static void index(OrdersCondition condition) {
        if (condition == null) {
            condition = new OrdersCondition();
        }
        //该商户ID
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<models.order.Order> orderList = models.order.Order.query(condition, supplierId, pageNumber, PAGE_SIZE);
        renderGoodsCond(condition);
        render(orderList);

    }

    /**
     * 商户订单详细
     *
     * @param orderNumber 订单编号
     */
    public static void details(String orderNumber) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        models.order.Order orders = models.order.Order.find("byOrderNumber", orderNumber).first();
        if (orders == null || orders.orderItems == null) {
            error("order can not find:" + orderNumber);
        }

        List<OrderItems> orderItems = new ArrayList<>();
        for (OrderItems orderItem : orders.orderItems) {
            if (orderItem.goods.supplierId.equals(supplierId)) {
                orderItems.add(orderItem);
            }
        }
        if (orderItems.size() == 0) {
            error(404, "该订单不存在！");
        }
        //收货信息
        render(orders, orderItems);
    }

    /**
     * 向页面设置选择信息
     *
     * @param goodsCond 页面设置选择信息
     */
    private static void renderGoodsCond(OrdersCondition goodsCond) {

        renderArgs.put("createdAtBegin", goodsCond.createdAtBegin);
        renderArgs.put("createdAtEnd", goodsCond.createdAtEnd);
        renderArgs.put("status", goodsCond.status);
        renderArgs.put("goodsName", goodsCond.goodsName);
        renderArgs.put("refundAtBegin", goodsCond.refundAtBegin);
        renderArgs.put("refundAtEnd", goodsCond.refundAtEnd);
        renderArgs.put("status", goodsCond.status);
        renderArgs.put("deliveryType", goodsCond.deliveryType);
        renderArgs.put("payMethod", goodsCond.payMethod);
        renderArgs.put("searchKey", goodsCond.searchKey);
        renderArgs.put("searchItems", goodsCond.searchItems);
    }

}
