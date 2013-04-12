package controllers;

import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 1.0版，只做维护
 */
@Deprecated
@With(SupplierRbac.class)
public class SupplierHome extends Controller {

    @ActiveNavigation("home")
    public static void index() {

        SupplierUser supplierUser = SupplierRbac.currentUser();

        if (supplierUser.supplier.getProperty(Supplier.SELL_ECOUPON)) {

        }

        // 如果跳转过新验证界面，使用之
        if ("v2".equals(supplierUser.defaultUiVersion)) {
            redirect("/ui-version/to/v2");
        }

        render();
    }

}
