package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 1.0版，只做维护
 */
@Deprecated
@With(SupplierRbac.class)
public class SupplierHome extends Controller {

    @ActiveNavigation("home")
    public static void index() {
        render();
    }

}
