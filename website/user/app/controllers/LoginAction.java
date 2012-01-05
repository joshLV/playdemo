package controllers;

import play.data.validation.Required;
import play.mvc.Controller;

/**
 * 
 * @author yanjy
 *
 */
public class LoginAction extends Controller{

	public static void index() {
		render("login/login.html");
	}

}
