package controllers;

import java.util.logging.Logger;

import play.mvc.Controller;

public class Home extends Controller {

    public static void index() {

        String title = new String("");

        System.out.println("lenght=" + title.length());

        render();
    }

}
