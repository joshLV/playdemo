package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

@Deprecated
@With(SupplierRbac.class)
public class SupplierReportsApplication extends Controller {

    @ActiveNavigation("finance_app")
    public static void index() {
        render();
    }
}
