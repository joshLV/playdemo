package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
public class OperateAdminApplication extends Controller {

    @ActiveNavigation("user_add")
    public static void index() {
        render();
    }

}