package controllers;

import models.consumer.User;
import models.operator.OperateUser;
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

@With({OperateRbac.class, ExcelControllerHelper.class})
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

    public static void updateExpress(Long id, OrderShippingInfo shippingInfo, String serviceRemarks) {
        OrderShippingInfo updateShippingInfo = OrderShippingInfo.findById(id);
        if (shippingInfo.expressCompany != null) {
            updateShippingInfo.expressCompany = shippingInfo.expressCompany;
        }
        if (StringUtils.isNotBlank(shippingInfo.expressNumber)) {
            updateShippingInfo.expressNumber = shippingInfo.expressNumber;
        }
        if (StringUtils.isNotBlank(shippingInfo.address)) {
            updateShippingInfo.address = shippingInfo.address;
        }
        if (StringUtils.isNotBlank(shippingInfo.phone)) {
            updateShippingInfo.phone = shippingInfo.phone;
        }
        updateShippingInfo.save();
        OrderItems orderItem = updateShippingInfo.orderItems.get(0);
        orderItem.order.serviceRemarks = serviceRemarks;
        orderItem.order.servicePerson = OperateRbac.currentUser().userName;
        if (orderItem.orderBatch != null) {
            orderItem.orderBatch.changedFlag = true;
            orderItem.orderBatch.save();
        }
        orderItem.order.save();
        orderItem.save();

        details(updateShippingInfo.orderItems.get(0).order.id);
    }


    @ActiveNavigation("order_index")
    public static void orderExcelOut(OrdersCondition condition) {
        condition = getOrdersCondition(condition);
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        condition.hasSeeAllSupplierPermission = ContextedPermission.hasPermission("SEE_ALL_SUPPLIER");
        condition.operatorId = OperateRbac.currentUser().id;
        List<Order> orderList = models.order.Order.query(condition, null, pageNumber, PAGE_SIZE);
        String resalerId = params.get("condition.resalerId");
        List<OrderItems> orderItems = new ArrayList<>();
        for (Order order : orderList) {
            OuterOrder outerOrder = OuterOrder.getOuterOrder(order);
            for (OrderItems orderItem : order.orderItems) {
                if (orderItem.status == OrderStatus.RETURNING) {
                    orderItem.orderItemStatus = "退货中";
                } else if (orderItem.status == OrderStatus.RETURNED) {
                    orderItem.orderItemStatus = "已退货";
                } else if (orderItem.status == OrderStatus.PAID) {
                    orderItem.orderItemStatus = "已付款";
                } else if (orderItem.status == OrderStatus.SENT) {
                    orderItem.orderItemStatus = "已发货";
                } else if (orderItem.status == OrderStatus.PREPARED) {
                    orderItem.orderItemStatus = "待打包";
                } else if (orderItem.status == OrderStatus.UPLOADED) {
                    orderItem.orderItemStatus = "已上传";
                } else if (orderItem.status == OrderStatus.CANCELED) {
                    orderItem.orderItemStatus = "交易关闭";
                } else {
                    orderItem.orderItemStatus = "未付款";
                }
                if (outerOrder != null) {
                    orderItem.orderShipOuterOrderId = outerOrder.orderId;
                } else {
                    orderItem.orderShipOuterOrderId = "";
                }
                orderItem.orderNumber = order.orderNumber;
                if (order.isWebsiteOrder()) {
                    orderItem.accountEmail = order.getUser().loginName;
                } else {
                    orderItem.accountEmail = order.getResaler().loginName + "-" + order.getResaler().userName;
                }
                orderItem.orderShipPaidAt = order.paidAt;
                if (orderItem.shippingInfo != null) {
                    orderItem.orderShipAddress = orderItem.shippingInfo.address;
                    orderItem.orderShipPhone = orderItem.shippingInfo.phone;
                    orderItem.orderShipOuterOrderId = orderItem.shippingInfo.outerOrderId;
                    orderItem.orderShipZipCode = orderItem.shippingInfo.zipCode;
                    orderItem.orderShipRemarks = orderItem.shippingInfo.remarks;
                    orderItem.orderShipExpressInfo = orderItem.shippingInfo.expressInfo;
                    orderItem.orderShipReceiver = orderItem.shippingInfo.receiver;
                }
                orderItem.orderNumber = orderItem.order.orderNumber;
                orderItem.orderShipPaidAt = orderItem.order.paidAt;
                orderItem.goodsCode = orderItem.goods.code;

                //导入非分销的订单处理
                if (StringUtils.isBlank(resalerId)) {
                    orderItem.orderShipPhone = orderItem.getMaskedPhone();
                    if (orderItem.getReturnEntry() != null) {
                        orderItem.returnedAt = orderItem.getReturnEntry().returnedAt;
                    } else {
                        orderItem.returnedAt = null;
                    }
                    orderItem.orderShipRemarks = order.remark;
                }

            }
            orderItems.addAll(order.orderItems);

        }

        request.format = "xls";
        renderArgs.put("__EXCEL_FILE_NAME__", "订单_" + System.currentTimeMillis() + ".xls");
        if (StringUtils.isNotBlank(resalerId)) {
            render("OperateOrders/realOrderExcelOut.xls", orderItems);
        }
        render(orderItems);

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
