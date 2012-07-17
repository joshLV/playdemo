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

    private static final Pattern hostPattern = Pattern.compile("(https?://)?([^/]*)(/?.*)");
  
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
    
    /**
     * 基于用户信息和cookie值创建或更新userWebIdentification
     * @param user
     * @param identificationValue
     * @return
     */
    private static UserWebIdentification findUserWebIdentification(
            final User user, final String identificationValue) {
        UserWebIdentification uwi = UserWebIdentification.findOne(identificationValue);
        if (uwi == null) {
            uwi = new UserWebIdentification();
            uwi.cookieId = identificationValue;
            uwi.user = user;
            uwi.firstPage = request.url;
            uwi.createdAt = new Date();
            uwi.referCode = request.params.get("tj");  //使用tj参数得到推荐码.
            Header header = request.headers.get("Referer");
            if (header != null) {
                uwi.referer = header.value();
                if (uwi.referer != null) {
                    uwi.refererHost = matchTheHostName(uwi.referer);
                }
            }
            uwi.save();
        } else {
            uwi.user = user;
            uwi.save();
        }
        return uwi;
    }

    /**
     * 从URL中匹配出主机名.
     * @param referer
     * @return
     */
    public static String matchTheHostName(String referer) {
        Matcher m = hostPattern.matcher(referer);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }    
}
