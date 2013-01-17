package controllers;

import models.order.PointGoodsOrder;
import models.order.PointGoodsOrderCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

//import models.order.Order;
//import models.order.OrderItems;
//import models.order.OrdersCondition;

/**
 * User: clara
 * Date: 12-8-8
 * Time: 下午2:48
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
    public static void index(PointGoodsOrderCondition condition) {
        if (condition == null) {
            condition = new PointGoodsOrderCondition();
        }
        String page = request.params.get("page");

        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);


        JPAExtPaginator<PointGoodsOrder> orderList =
                PointGoodsOrder.query(condition, pageNumber, PAGE_SIZE);

//        BigDecimal amountSummary = PointGoodsOrder.findUserTotalPoint  (orderList);
//        List<Brand> brandList = Brand.findByOrder(null);
//        renderArgs.put("brandList", brandList);
//        render(orderList, condition, amountSummary);
        render(orderList, condition);

    }

    /**
     * 审核.
     */
    //Long id,     PointGoodsOrder order
    public static void send(Long id, PointGoodsOrder pointGoodsOrder) {
        PointGoodsOrder originalOrder = PointGoodsOrder.findById(id);
        if (originalOrder == null) {
            error(500, "can not deal with the order:" + id);
        }

//        System.out.println("originalOrder.note"+originalOrder.note);

        if (pointGoodsOrder.note == null || pointGoodsOrder.note.trim().isEmpty()) {
            PointGoodsOrder.acceptOrder(id);
        } else {
            PointGoodsOrder.cancelOrder(id, pointGoodsOrder.note);
        }
        index(null);
    }

    /**
     * 订单发货
     *
     * @param id
     * @param pointGoodsOrder
     */


    public static void sendGoods(Long id, PointGoodsOrder pointGoodsOrder) {
        PointGoodsOrder originalOrder = PointGoodsOrder.findById(id);
        if (originalOrder == null) {
            error(500, "can not deal with the order:" + id);
        }

        PointGoodsOrder.sendGoods(id, pointGoodsOrder.note);

        index(null);
    }


    /**
     * 积分商品订单详细
     *
     * @param id 订单ID
     */
    public static void details(Long id) {
        //订单信息
        models.order.PointGoodsOrder pointGoodsOrder = models.order.PointGoodsOrder.findById(id);
//        List<OrderItems> orderItems = orders.orderItems;
        //收货信息
//        render(orders, orderItems);
        render(pointGoodsOrder);
    }


    public static void orderExcelOut(PointGoodsOrderCondition condition) {

        if (condition == null) {
            condition = new PointGoodsOrderCondition();
        }
        String page = request.params.get("page");
        request.format = "xls";
        String __EXCEL_FILE_NAME__ = "订单_" + System.currentTimeMillis() + "xls";
        renderArgs.put("__EXCEL_FILE_NAME__", __EXCEL_FILE_NAME__);
//        JPAExtPaginator<models.order.Order> orderList = models.order.Order.query(condition, null, 1, PAGE_SIZE);
//        for (PointGoodsOrder order : orderList) {
//            if (order.userType == AccountType.CONSUMER) {
//                order.accountEmail = order.getUser().loginName;
//            } else if (order.userType == AccountType.RESALER) {
//                order.accountEmail = order.getResaler().loginName;
//            }
//            order.save();
//        }
//        render(__EXCEL_FILE_NAME__, orderList);

    }


}
