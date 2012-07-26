package controllers;

import java.util.*;

import models.*;

import operate.rbac.annotations.ActiveNavigation;

import play.*;
import play.mvc.*;

@With(OperateRbac.class)
@ActiveNavigation("reports_app")
public class Application extends Controller {

    public static void index() {
        render();
    }

}
