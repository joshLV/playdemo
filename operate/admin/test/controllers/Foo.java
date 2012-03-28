package controllers;

import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import play.mvc.Controller;
import play.mvc.With;
import controllers.operate.cas.SecureCAS;


@With({SecureCAS.class, MenuInjector.class})
@Right("PERM_TEST")
@ActiveNavigation("bar")
public class Foo extends Controller {
    
    public static void bar() {
        ok();
    }
}
