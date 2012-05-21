package controllers;

import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With(SupplierRbac.class)

public class SupplierProfiles extends Controller {

    /**
     * 用户信息
     */
    public static void index() {
        Long id = SupplierRbac.currentUser().id;
        SupplierUser supplierUser = SupplierUser.findById(id);
        String roleIds = "";
        if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
            for (SupplierRole role : supplierUser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = SupplierRole.findRoleOrderById();
        supplierUser.roles.addAll(rolesList);
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List shopList = Shop.findShopBySupplier(supplierId);
        String okFlag = "ok";
        render(supplierUser, roleIds, rolesList,shopList,okFlag);

    }

}
