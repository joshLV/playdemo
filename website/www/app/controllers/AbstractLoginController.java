package controllers;

import models.consumer.User;
import play.mvc.Controller;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/17/12
 * Time: 10:41 AM
 */
public class AbstractLoginController extends Controller {

    public static User getUser() {
        String username = session.get("username");
        return User.find("byLoginName", username).first();
    }

}
