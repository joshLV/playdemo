package controllers.data;

import controllers.OperateRbac;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
public class SingleFoo extends Controller {

    @Right("PERM_TEST")
    @ActiveNavigation("bar")
    public static void bar() {
        ok();
    }

    @Right("ROLE_ADD")
    @ActiveNavigation("user_add")
    public static void user() {
        ok();
    }
    
    @ActiveNavigation("google")
    public static void google() {
        ok();
    }
}
