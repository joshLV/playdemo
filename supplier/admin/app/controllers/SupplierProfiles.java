package controllers;

import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import play.data.validation.Valid;
import play.data.validation.Validation;
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
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        List shopList = Shop.findShopBySupplier(supplierId);
        render(supplierUser, roleIds, shopList);
    }

    /**
     * 操作员信息修改
     *
     * @param id           ID   不应当通过ID修改对象，应当通过session得到
     * @param supplierUser 用户信息
     */
    public static void update(Long id, @Valid SupplierUser supplierUser) {
        Long userId = SupplierRbac.currentUser().id;
        checkValid(userId, supplierUser);
        // 更新用户信息
        SupplierUser.update(userId, supplierUser);
        index();
    }

    /**
     * 验证
     *
     * @param supplierUser 操作员信息
     */
    private static void checkValid(Long id, SupplierUser supplierUser) {
        Validation.match("validation.jobNumber", supplierUser.jobNumber, "^[0-9]*");
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
                for (SupplierRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            supplierUser.id = id;
            render("SupplierProfiles/index.html", supplierUser, roleIds, rolesList);
        }
    }

}
