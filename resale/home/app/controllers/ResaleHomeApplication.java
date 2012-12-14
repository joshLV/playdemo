package controllers;

import controllers.modules.resale.cas.SecureCAS;
import controllers.modules.resale.cas.annotations.SkipCAS;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
@SkipCAS
public class ResaleHomeApplication extends Controller {

    public static void index() {
        render();
    }
    public static void solution() {
        render();
    }

}
