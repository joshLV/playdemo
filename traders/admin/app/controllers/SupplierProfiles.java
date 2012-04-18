package controllers;

import models.admin.SupplierUser;
import navigation.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
@With(SupplierRbac.class)
@ActiveNavigation("user_search")
public class SupplierProfiles extends Controller {

    /**
     * 用户信息
     */
    public static void index() {
        render();
    }

}
