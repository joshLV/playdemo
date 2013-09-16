package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.ContextedPermission;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.codec.digest.DigestUtils;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

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

    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<SupplierRole> roles = new ArrayList<>();
        SupplierRole role = SupplierRole.findById(1L);
        roles.add(role);
        role = SupplierRole.findById(2L);
        roles.add(role);
        role = SupplierRole.findById(4L);
        roles.add(role);
        role = SupplierRole.findById(5L);
        roles.add(role);
        for (Supplier supplier : supplierList) {
            SupplierUser supplierUser = new SupplierUser();

            String password_salt = "'*(Y*@#&Y#'";
            // 密码加密
            supplierUser.encryptedPassword = DigestUtils.md5Hex("Yunying.2013" + password_salt);
            supplierUser.roles = roles;
            // 随机码
            supplierUser.passwordSalt = password_salt;
            supplierUser.lockVersion = 0;

            supplierUser.supplier = supplier;
            supplierUser.loginName = "yy585".trim();

            supplierUser.deleted = DeletedStatus.UN_DELETED;
            supplierUser.save();
        }

    }

}
