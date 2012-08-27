package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@With(SupplierRbac.class)
public class Application extends Controller {

    @ActiveNavigation("user_add")
    public static void index() {
        render();
    }

}