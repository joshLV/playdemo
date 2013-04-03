package controllers.real;

import controllers.OperateRbac;
import models.order.PurchaseOrder;
import models.order.Vendor;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购合同管理
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午2:53
 */
@With(OperateRbac.class)
@ActiveNavigation("purchase_orders_index")
public class PurchaseOrders extends Controller {

    @ActiveNavigation("purchase_orders_index")
    public static void index(String keyword) {
        int page = getPage();
        List<PurchaseOrder> purchaseOrderList = PurchaseOrder.findByCondition(keyword);
        render(purchaseOrderList, page, keyword);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    @ActiveNavigation("purchase_orders_add")
    public static void add() {
        List<Vendor> vendorList = Vendor.findUnDeleted();
        render(vendorList);
    }

    @ActiveNavigation("vendors_add")
    public static void create(@Valid PurchaseOrder purchaseOrder, List<Long> count) {
        System.out.println(count + "===count>>");
        if (Validation.hasErrors()) {
            render("real/PurchaseOrders/add.html");
        }
        purchaseOrder.createdBy = OperateRbac.currentUser().userName;
        purchaseOrder.create();
        index(null);
    }

    public static void edit(Long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        List<Vendor> vendorList = Vendor.findUnDeleted();
        render(purchaseOrder, vendorList);
    }


    public static void update(Long id, @Valid Vendor vendor) {
        if (Validation.hasErrors()) {
            render("real/Vendors/edit.html", vendor, id);
        }
        Vendor.update(id, vendor);
        index(null);
    }
}
