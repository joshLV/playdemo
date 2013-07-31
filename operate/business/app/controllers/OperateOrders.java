package controllers;

import models.consumer.User;
import models.order.*;
import models.resale.Resaler;
import models.sales.Brand;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@With(OperateRbac.class)
@ActiveNavigation("order_index")
public class OperateOrders extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 商户订单信息一览
     *
     * @param condition 页面条件信息
     */
    public static void index(OrdersCondition condition, String desc) {
        condition = getOrdersCondition(condition);

        // DESC 的值表示升降序，含7位，代表7个排序字段（不含订单编号,商品名称）， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 -1
        if (desc == null) {
            desc = "-1";
        }

        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();

        if (isValidDesc(desc)) {
            //排序合法且没有优先指数，添加到condition 中
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    break;
                }
            }
            String[] orderBy = {"o.status", "o.userId", "o.userType", "o.amount", "o.createdAt", "o.paidAt", "o.refundAt"};
            // 添加排序属性
            condition.orderBy = orderBy[index];
            // 添加升降序方式
            if (desc.charAt(index) == '1') {
                condition.orderByType = "asc";
            } else {
                condition.orderByType = "desc";
            }

        } else {
            // 一般排序，按创建时间
            condition.orderBy = "o.createdAt";
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        condition.operatorId = OperateRbac.currentUser().id;
        Long operatorId = OperateRbac.currentUser().id;
        Boolean hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        JPAExtPaginator<models.order.Order> orderList;

        orderList = models.order.Order.query(condition, null, pageNumber, PAGE_SIZE);
        BigDecimal amountSummary = Order.summary(orderList);
        List<Brand> brandList = Brand.findByOrder(null, operatorId, hasSeeAllSupplierPermission);
        renderArgs.put("brandList", brandList);
        render(orderList, condition, amountSummary, desc);

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
        index(null, "");
    }

    /**
     * 商户订单详细
     *
     * @param id 订单ID
     */
    public static void details(Long id) {
        //订单信息
        models.order.Order orders = models.order.Order.findById(id);
        List<OrderItems> orderItems = orders.orderItems;
        //收货信息
        String loginName = "";
        if (orders.isWebsiteOrder()) {
            User user = User.findById(orders.consumerId);
            if (user != null) {
                loginName = user.loginName;
            }
        } else {
            Resaler resaler = Resaler.findById(orders.userId);
            if (resaler != null) {
                loginName = resaler.loginName;
            }
        }

        // 用于查看手机号的权限
        Boolean hasViewEcouponSnPermission = ContextedPermission.hasPermission("VIEW_ECOUPONSN");
        List<ExpressCompany> expressList = ExpressCompany.findAll();
        render(orders, orderItems, loginName, hasViewEcouponSnPermission, expressList);
    }

    public static void updateExpress(Long id, OrderShippingInfo shippingInfo) {
        OrderShippingInfo updateShippingInfo = OrderShippingInfo.findById(id);
        updateShippingInfo.expressCompany = shippingInfo.expressCompany;
        updateShippingInfo.expressNumber = shippingInfo.expressNumber;
        updateShippingInfo.save();
        details(updateShippingInfo.orderItems.get(0).order.id);
    }


    public static void orderExcelOut(OrdersCondition condition) {
        condition = getOrdersCondition(condition);
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        condition.operatorId = OperateRbac.currentUser().id;
        List<Order> orderList = models.order.Order.query(condition, null, pageNumber, PAGE_SIZE);
        String resalerId = params.get("condition.resalerId");
        for (Order order : orderList) {
            OuterOrder outerOrder = OuterOrder.getOuterOrder(order);
            if (outerOrder != null) {
                order.outerOrderNumber = outerOrder.orderId;
            } else {
                order.outerOrderNumber = "";
            }
            if (StringUtils.isNotBlank(resalerId)) {
                for (OrderItems orderItem : order.orderItems) {
                    if (orderItem.status == OrderStatus.RETURNING) {
                        order.orderItemStatus = "退货中";
                    } else if (orderItem.status == OrderStatus.RETURNED) {
                        order.orderItemStatus = "已退货";
                    } else if (orderItem.status == OrderStatus.PAID) {
                        order.orderItemStatus = "已付款";
                    } else if (orderItem.status == OrderStatus.SENT) {
                        order.orderItemStatus = "已发货";
                    } else if (orderItem.status == OrderStatus.PREPARED) {
                        order.orderItemStatus = "已上传";
                    }
                }

            }
            if (order.isWebsiteOrder()) {
                order.accountEmail = order.getUser().loginName;
            } else {
                order.accountEmail = order.getResaler().loginName + "-" + order.getResaler().userName;
            }
        }

        if (StringUtils.isNotBlank(resalerId)) {
            render("OperateOrders/realOrderExcelOut.xls", orderList);
        }
        render(orderList);

    }

    private static OrdersCondition getOrdersCondition(OrdersCondition condition) {

        if (condition == null) {
            condition = new OrdersCondition();
            condition.hidPaidAtBegin = DateHelper.beforeDays(1);
            condition.hidPaidAtEnd = new Date();
        } else if (condition.shihuiSupplierId == null &&
                (StringUtils.isBlank(condition.searchKey) || StringUtils.isBlank(condition.searchItems)) &&
                StringUtils.isBlank(condition.outerOrderId) && condition.paidAtBegin == null && condition.paidAtEnd == null) {
            condition.paidAtBegin = DateHelper.beforeDays(1);
            condition.paidAtEnd = new Date();
        }
        return condition;
    }

    /**
     * 判断排序字符串的合法性
     *
     * @param desc 排序字符串
     * @return
     */
    public static boolean isValidDesc(String desc) {
        if (desc.length() != 7) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 6) {
            return false;
        }
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) != '0' && desc.charAt(i) != '1' && desc.charAt(i) != '2') {
                return false;
            }
        }
        return true;
    }
}
