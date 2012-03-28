package controllers;

import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;
import controllers.operate.cas.SecureCAS;


@With({SecureCAS.class, OperateRbac.class})
@Right("PERM_TEST")
@ActiveNavigation("bar")
public class Foo extends Controller {
    
    public static void bar() {
        ok();
    }
}
