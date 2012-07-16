package controllers;

import java.util.List;
import java.util.UUID;

import models.cms.FriendsLink;
import models.consumer.User;
import models.order.Cart;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;

public class WebsiteInjector extends Controller {

    private static String baseDomain = play.Play.configuration.getProperty("application.baseDomain");

    public static final String WEB_TRACK_COOKIE = "ybq_track";

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

        //友情链接
        List<FriendsLink> friendsLinks = CacheHelper.getCache(CacheHelper.getCacheKey(FriendsLink.CACHEKEY, "FRIENDS_LINK"), new CacheCallBack<List<FriendsLink>>() {
            @Override
            public List<FriendsLink> loadData() {
                return FriendsLink.findAllByDeleted();
            }
        });
        renderArgs.put("friendsLinks", friendsLinks);
    }

    @Before
    public static void injectWebIdentification() {

        final User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        if (cookie == null) {
            String uuid = UUID.randomUUID().toString();
            //cookie = response.setCookie(name, value, domain, path, maxAge, secure);
        }
        final String cookieValue = cookie.value;


    }
}
