package controllers;

import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;

@With(SupplierRbac.class)
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
