package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
public class Application extends Controller {

    
    @ActiveNavigation("account_app")
    public static void account() {
        render();
    }
}
