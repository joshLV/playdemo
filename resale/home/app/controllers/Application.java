package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

import models.*;

@With({SecureCAS.class ,ResaleCAS.class})
public class Application extends Controller {

    public static void index() {
        System.out.print("h");
    }

}