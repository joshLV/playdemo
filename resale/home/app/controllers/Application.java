package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class Application extends Controller {

    public static void index() {
        render();
    }

}
