package controllers;

import java.util.List;
import models.consumer.User;
import models.order.Cart;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import controllers.modules.website.cas.SecureCAS;

public class WebsiteInjector extends Controller {
    
	@Before
	public static void injectCarts() {
        User user = SecureCAS.getUser();
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);
        renderArgs.put("carts", carts);
	}
}
