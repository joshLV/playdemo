package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class Application extends Controller {

    public static void index() {
    }

}