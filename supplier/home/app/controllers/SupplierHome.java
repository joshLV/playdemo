package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(SupplierRbac.class)
public class SupplierHome extends Controller {

    @ActiveNavigation("home")
    public static void index() {
        render();
    }

}
