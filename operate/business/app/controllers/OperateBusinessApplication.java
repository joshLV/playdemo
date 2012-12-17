package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
public class OperateBusinessApplication extends Controller {

    /**
     * 财务管理的默认页面.
     */
    @ActiveNavigation("account_app")
    public static void account() {
        render();
    }
}
