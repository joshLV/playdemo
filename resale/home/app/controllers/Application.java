package controllers;

import controllers.modules.resale.cas.annotations.SkipCAS;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
@SkipCAS
public class Application extends Controller {

    public static void index() {
        render();
    }
    public static void solution() {
        render();
    }

}
