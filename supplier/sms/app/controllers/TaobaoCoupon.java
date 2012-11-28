package controllers;

import play.mvc.Controller;

/**
 * @author likang
 *         Date: 12-11-27
 */
public class TaobaoCoupon extends Controller{
    public static void index(String method) {

        switch (method){
            case "send":
                send();
                break;
            case "b":
                break;
            default:
                throw new IllegalArgumentException("no such method");
        }
    }

    public static void send() {

    }
}
