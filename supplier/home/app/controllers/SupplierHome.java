package controllers;

import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.ContextedPermission;
import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 1.0版，只做维护
 */
@With(SupplierRbac.class)
public class SupplierHome extends Controller {

    @ActiveNavigation("home")
    public static void index() {

        SupplierUser supplierUser = SupplierRbac.currentUser();

        // 有电子券销售
        if ("1".equals(supplierUser.supplier.getProperty(Supplier.SELL_ECOUPON))) {
            if (ContextedPermission.hasPermission("COUPON_MULTI_VERIFY")) {
                redirect("/coupons");
            }

            if (ContextedPermission.hasPermission("ACCOUNT_REPORT")) {
                redirect("/sequences");
            }
            redirect("/shops");
        }

        // 只有实物类
        if ("1".equals(supplierUser.supplier.getProperty(Supplier.CAN_SALE_REAL))) {
            if (ContextedPermission.hasPermission("DOWNLOAD_ORDER_SHIPPING")) {
                redirect("/real/download-order-shipping");
            }
            if (ContextedPermission.hasPermission("ACCOUNT_REPORT")) {
                redirect("/sequences");
            }
            redirect("/shops");
        }

        // 考虑兼容性，默认到电子券首页
        redirect("/verify");
    }

}
