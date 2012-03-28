package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.operate.cas.SecureCAS;

@With({SecureCAS.class, MenuInjector.class})
public class Application extends Controller {

    @ActiveNavigation("user_add")
    public static void index() {
        render();
    }

}