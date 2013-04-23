package controllers.supplier;

import controllers.SupplierRbac;
import models.admin.SupplierUser;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import play.supplier.cas.CASUtils;

/**
 * User: tanglq
 * Date: 12-12-27
 * Time: 下午5:01
 */
@With(SupplierRbac.class)
public class ChangeUIVersions extends Controller {

    public static void execute(String version) {

        String baseDomain = Play.configuration.getProperty("application.baseDomain", "quanmx.com");

        StringBuffer sbUrl = new StringBuffer("http://");
        sbUrl.append(CASUtils.getSubDomain());
        SupplierUser supplierUser = SupplierRbac.currentUser();

        if ("v1".equals(version)) {
            sbUrl.append(".order.").append(baseDomain)
                    .append("/coupons/single");
            supplierUser.defaultUiVersion = "v1";
        } else {
            // 跳转到新版本 v2
            sbUrl.append(".home.").append(baseDomain)
                    .append("/");
            supplierUser.defaultUiVersion = "v2";
        }

        supplierUser.save();

        redirect(sbUrl.toString());
    }

}
