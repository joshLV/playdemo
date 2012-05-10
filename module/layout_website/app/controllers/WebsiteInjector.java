package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.order.Cart;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;

import java.util.List;

public class WebsiteInjector extends Controller {

    @Before
    public static void injectCarts() {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);
        int count = 0;
        for (Cart cart : carts) {
            count += cart.number;
        }
        renderArgs.put("carts", carts);
        renderArgs.put("count", count);
    }
}
