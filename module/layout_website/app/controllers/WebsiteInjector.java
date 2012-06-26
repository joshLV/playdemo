package controllers;

import java.util.List;
import models.consumer.User;
import models.order.Cart;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;

public class WebsiteInjector extends Controller {

    @Before
    public static void injectCarts() {
        final User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        final String cookieValue = cookie == null ? null : cookie.value;

        Integer cart_size = CacheHelper.getCache(Cart.getCartCacheKey(user, cookieValue), new CacheCallBack<Integer>() {
            @Override
            public Integer loadData() {
                List<Cart> carts = Cart.findAll(user, cookieValue);
                int count = 0;
                for (Cart cart : carts) {
                    count += cart.number;
                }                
                return new Integer(count);
            }
        });
        renderArgs.put("count", cart_size);
    }
}
