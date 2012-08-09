package controllers;

import models.accounts.AccountType;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrdersCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-8
 * Time: 下午2:48
 * To change this template use File | Settings | File Templates.
 */


@With(OperateRbac.class)
@ActiveNavigation("point_goods_order")
public class OperatePointGoodsOrders extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 积分商品订单信息一览
     *
     * @param condition 页面条件信息
     */
    public static void index(OrdersCondition condition) {
        if (condition == null) {
            condition = new OrdersCondition();
        }
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<Order> orderList = models.order.Order.query(condition, null, pageNumber, PAGE_SIZE);

        BigDecimal amountSummary = Order.summary(orderList);
//        List<Brand> brandList = Brand.findByOrder(null);
//        renderArgs.put("brandList", brandList);
        render(orderList, condition, amountSummary);

    }

    /**
     * 订单发货.
     */
    public static void send(Long id, Order order) {
        Order originalOrder = Order.findById(id);
        if (originalOrder == null) {
            error(500, "can not find the order:" + id);
        }
        Order.sendRealGoodsAndPayCommissions(id, order.deliveryCompany, order.deliveryNo);
        index(null);
    }

    /**
     * 积分商品订单详细
     *
     * @param id 订单ID
     */
    public static void details(Long id) {
        //订单信息
        models.order.Order orders = models.order.Order.findById(id);
        List<OrderItems> orderItems = orders.orderItems;
        //收货信息
        render(orders, orderItems);
    }


    public static void orderExcelOut(OrdersCondition condition) {

        if (condition == null) {
            condition = new OrdersCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        String __EXCEL_FILE_NAME__ = "订单_" + System.currentTimeMillis() + "xls";
        renderArgs.put("__EXCEL_FILE_NAME__", __EXCEL_FILE_NAME__);
        JPAExtPaginator<models.order.Order> orderList = models.order.Order.query(condition, null, 1, PAGE_SIZE);
        for (Order order : orderList) {
            if (order.userType == AccountType.CONSUMER) {
                order.accountEmail = order.getUser().loginName;
            } else if (order.userType == AccountType.RESALER) {
                order.accountEmail = order.getResaler().loginName;
            }
//            order.save();
        }
        render(__EXCEL_FILE_NAME__, orderList);

    }

}
