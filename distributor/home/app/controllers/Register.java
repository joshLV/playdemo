package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.modules.webcas.WebCAS;
/**
 * 分销商注册
 * 
 * @author yanjy
 *
 */
public class Register extends Controller {

	/**
	 * 注册页面
	 */
	public static void index(){
		render();
	}
	
	public static void create(){
		render();
	}
}
