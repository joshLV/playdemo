package controllers;

import play.*;
import play.mvc.*;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;

import java.util.*;

import models.*;

@With({MenuInjector.class})
public class Application extends Controller {

    @ActiveNavigation("home")
    public static void index() {
        render();
    }

}