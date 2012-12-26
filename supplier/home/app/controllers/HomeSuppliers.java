package controllers;

import models.sales.Brand;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 商户信息控制器.
 * 1.0版，只做维护
 * <p/>
 * User: sujie
 * Date: 4/17/12
 * Time: 4:16 PM
 */
@Deprecated
@With(SupplierRbac.class)
public class HomeSuppliers extends Controller {

    @ActiveNavigation("suppliers_show")
    public static void show() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);

        render(supplier);
    }

}