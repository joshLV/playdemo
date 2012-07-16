package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import models.cms.FriendsLink;
import models.consumer.User;
import models.consumer.UserWebIdentification;
import models.order.Cart;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;
import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;

public class WebsiteInjector extends Controller {

    private static String baseDomain = play.Play.configuration.getProperty("application.baseDomain");

    public static final String WEB_TRACK_COOKIE = "ybq_track";

    private static final Pattern hostPattern = Pattern.compile("http(s?):\\/\\/([^\\/\\:]*)");
  
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
        String cookieValue = null;
        if (cookie == null) {
            cookieValue = UUID.randomUUID().toString();
            response.setCookie(WEB_TRACK_COOKIE, cookieValue, baseDomain, "/", -1, false);
        } else {
            cookieValue = cookie.value;
        }
        
        final String identificationValue = cookieValue;
        final Long userId = user != null ? user.getId() : 0l;
        UserWebIdentification identification = CacheHelper.getCache("WEBIDENTI_" + identificationValue + "_" + userId, new CacheCallBack<UserWebIdentification>() {
            @Override
            public UserWebIdentification loadData() {
                return findUserWebIdentification(user, identificationValue);
            }
        });
        renderArgs.put("userWebIdentification", identification);
    }
    

    private static UserWebIdentification findUserWebIdentification(
            final User user, final String identificationValue) {
        UserWebIdentification uwi = UserWebIdentification.findOne(identificationValue);
        if (uwi == null) {
            uwi = new UserWebIdentification();
            uwi.cookieId = identificationValue;
            uwi.user = user;
            uwi.createdAt = new Date();
            Header header = request.headers.get("Referer");
            System.out.println("header:" + header);
            if (header != null) {
                
            System.out.println("header.value:" + header.value());
                uwi.referer = header.value();
                if (uwi.referer != null) {
                    Matcher m = hostPattern.matcher(uwi.referer);
                    if (m.matches()) {
                        System.out.println("match================");
                        uwi.refererHost = m.group(1);
                    } else {
                        System.out.println("NOT matcher++++++++++++++++");
                    }
                }
            }
            uwi.save();
        } else {
            uwi.user = user;
            uwi.save();
        }
        return uwi;
    }    
}
