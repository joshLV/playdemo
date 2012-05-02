package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
@ActiveNavigation("cms_main")
public class Application extends Controller {

    public static void index() {
        render();
    }

}