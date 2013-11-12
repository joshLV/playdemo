package controllers;

import models.operator.OperateUser;
import models.order.OrderItems;
import models.order.OrderShippingInfo;
import models.order.OrderStatus;
import models.sales.OrderBatch;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 待发货清单下载
 * <p/>
 * User: yanjy
 * Date: 13-3-13
 * Time: 上午11:14
 */
@With({OperateRbac.class, ExcelControllerHelper.class})
@ActiveNavigation("download_order_shipping_index")
public class DownloadOrderShippingInfos extends Controller {
    public static int PAGE_SIZE = 10;
    public static final String EXCEL = "xls";

    @ActiveNavigation("download_order_shipping_index")
    public static void index(Long supplierId) {
        int pageNumber = getPageNumber();
        List<Supplier> supplierList = Supplier.findSuppliersByCanSaleReal();
        if (supplierId == null && supplierList.size() > 0) {
            supplierId = Supplier.getShihui().id;
        }
        Supplier supplier = Supplier.findById(supplierId);
        if (supplier.canSaleReal == null || !supplier.canSaleReal) {
            error("have no real goods!");
        }
        List<OrderItems> orderItemsList = getPreparedItems(null, supplierId);
        ModelPaginator<OrderBatch> orderBatchList = OrderBatch.findBySupplier(supplierId, pageNumber, PAGE_SIZE);
        render(orderItemsList, orderBatchList, supplierList, supplierId);
    }

    /**
     * 取得待打包清单
     *
     * @return
     */
    private static List<OrderItems> getPreparedItems(Long orderBatchId, Long supplierId) {
        StringBuilder sql = new StringBuilder("goods.supplierId=? and goods.sku is not null and status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(supplierId);
        params.add(OrderStatus.PREPARED);
        if (orderBatchId == null) {
            sql.append(" and orderBatch is null");
        } else {
            sql.append(" and orderBatch.id= ?");
            params.add(orderBatchId);
        }
        return OrderItems.find(sql.toString(), params.toArray()).fetch();
    }

    /**
     * 导出发货单
     */
    public static void exportOrderShipping(Long id, Long supplierId) {
        OperateUser operateUser = OperateRbac.currentUser();
        List<OrderItems> orderItems = getPreparedItems(id, supplierId);
        Supplier supplier = Supplier.findUnDeletedById(supplierId);
        List<OrderItems> orderItemsList = new ArrayList<>();
        for (OrderItems orderItem : orderItems) {
            orderItem.orderShipAddress = orderItem.shippingInfo.address;
            orderItem.orderShipPhone = orderItem.shippingInfo.phone;
            orderItem.orderShipOuterOrderId = orderItem.shippingInfo.outerOrderId;
            orderItem.orderShipZipCode = orderItem.shippingInfo.zipCode;
            orderItem.orderShipRemarks = orderItem.shippingInfo.remarks;
            orderItem.orderShipExpressInfo = orderItem.shippingInfo.expressInfo;
            orderItem.orderShipReceiver = orderItem.shippingInfo.receiver;
            orderItem.orderNumber = orderItem.order.orderNumber;
            orderItem.orderShipPaidAt = orderItem.order.paidAt;
            orderItem.goodsCode = orderItem.goods.code;
            orderItemsList.add(orderItem);

        }
        if (id == null) {
            OrderBatch orderBatch = new OrderBatch(supplier, operateUser.userName, Long.parseLong(String.valueOf(orderItemsList.size()))).save();
            id = orderBatch.id;
            //更新orderItems的状态为：代打包
            for (OrderItems orderItem : orderItems) {
                orderItem.status = OrderStatus.PREPARED;
                orderItem.orderBatch = orderBatch;
                orderItem.save();
            }
        }

        request.format = EXCEL;
        renderArgs.put("__EXCEL_FILE_NAME_", "发货单_" + supplier.getName() + "_" + id + "." + EXCEL);
        render(orderItemsList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
