package controllers;

import navigation.annotations.ActiveNavigation;
import java.util.logging.Logger;
import play.mvc.With;

import play.mvc.Controller;

@With({MenuInjector.class})
public class Home extends Controller {

    @ActiveNavigation("home")
    public static void index() {
        render();
    }



}
