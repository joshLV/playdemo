package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
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
        final User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        final String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = CacheHelper.getCache(Cart.getCartCacheKey(user, cookieValue), new CacheCallBack<List<Cart>>() {
            @Override
            public List<Cart> loadData() {
                return Cart.findAll(user, cookieValue);
            }
        });

        int count = 0;
        for (Cart cart : carts) {
            count += cart.number;
        }

        renderArgs.put("carts", carts);
        renderArgs.put("count", count);

    }
}
