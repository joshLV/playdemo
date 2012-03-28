package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.operate.cas.SecureCAS;

@With({SecureCAS.class, OperateRbac.class})
public class Application extends Controller {

    @ActiveNavigation("user_add")
    public static void index() {
        render();
    }

}