package controllers;

import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;

@With(SupplierRbac.class)
public class Home extends Controller {

    @ActiveNavigation("home")
    @Right("HOME")
    public static void index() {
        render();
    }

}
