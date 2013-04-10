package controllers.data;

import controllers.OperateRbac;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
@Right("PERM_TEST")
@ActiveNavigation("bar")
public class Foo extends Controller {
    
    public static void bar() {
        ok();
    }
}
