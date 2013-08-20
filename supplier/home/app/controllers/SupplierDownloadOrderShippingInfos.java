package controllers;

import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.RealGoodsReturnEntry;
import models.sales.OrderBatch;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ModelPaginator;
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
public class SupplierDownloadOrderShippingInfos extends Controller {
    public static int PAGE_SIZE = 10;
    public static final String EXCEL = "xls";

    public static void index() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        //先检查是否有退货订单，如果有待处理的退货订单必须先处理掉再做出库操作。以确保库存的准确的情况下正确出库。
        long returnEntryCount = RealGoodsReturnEntry.countHandling(supplier.id);

        int pageNumber = getPageNumber();
        if (!"1".equals(supplier.getProperty(Supplier.CAN_SALE_REAL))) {
            error("have no real goods!");
        }
        List<OrderItems> orderItemsList = getPreparedItems(null);
        ModelPaginator<OrderBatch> orderBatchList = OrderBatch.findBySupplier(supplier.id, pageNumber, PAGE_SIZE);
        render(orderItemsList, orderBatchList, returnEntryCount);
    }

    /**
     * 取得待打包清单
     *
     * @return
     */
    private static List<OrderItems> getPreparedItems(Long orderBatchId) {
//        StringBuilder sql = new StringBuilder("goods.supplierId=? and goods.sku is not null ");
        StringBuilder sql = new StringBuilder("goods.material_type ='REAL' and goods.supplierId=?");
        List<Object> params = new ArrayList();
        params.add(SupplierRbac.currentUser().supplier.id);
        if (orderBatchId == null) {
            sql.append(" and orderBatch is null");
        } else {
            sql.append(" and orderBatch.id = ?");
            params.add(orderBatchId);
        }
        return OrderItems.find(sql.toString(), params.toArray()).fetch();
    }

    /**
     * 导出发货单
     */
    public static void exportOrderShipping(Long id) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        List<OrderItems> orderItemsList = getPreparedItems(id);
        if (id == null) {
            OrderBatch orderBatch = new OrderBatch(supplierUser.supplier, supplierUser.userName, Long.parseLong(String.valueOf(orderItemsList.size()))).save();
            id = orderBatch.id;
            //更新orderItems的状态为：代打包
            for (OrderItems orderItems : orderItemsList) {
                orderItems.status = OrderStatus.PREPARED;
                orderItems.orderBatch = orderBatch;
                orderItems.save();
            }
        }
        request.format = EXCEL;
        renderArgs.put("__FILE_NAME__", "发货单_" + id + "." + EXCEL);
        render(orderItemsList);
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
