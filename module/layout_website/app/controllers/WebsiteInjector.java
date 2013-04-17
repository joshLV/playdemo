package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.PlatformType;
import controllers.modules.website.cas.SecureCAS;
import models.cms.Block;
import models.cms.BlockType;
import models.cms.Topic;
import models.cms.TopicType;
import models.consumer.User;
import models.consumer.UserWebIdentification;
import models.order.Cart;
import models.order.Order;
import models.sales.Area;
import models.sales.Category;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsiteInjector extends Controller {

    private static String baseDomain = play.Play.configuration.getProperty("application.baseDomain");

    public static final String WEB_TRACK_COOKIE = "ybq_track";

    public static final String PROMOTER_COOKIE = "promoter_track";

    private static final Pattern hostPattern = Pattern.compile("(https?://)?([^/]*)(/?.*)");

    private static ThreadLocal<UserWebIdentification> _userWebIdentification = new ThreadLocal<>();

    @Before
    public static void injectCarts() {
        CacheHelper.preRead(CacheHelper.getCacheKey(models.sales.Category.CACHEKEY, "WWW_TOP_CATEGORIES7"),
                CacheHelper.getCacheKey(models.sales.Category.CACHEKEY, "WWW_TOP_LEFT_CATEGORIES5"),
                CacheHelper.getCacheKey(models.sales.Area.CACHEKEY, "WWW_AREAS6_" + Area.SHANGHAI),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_TOP_HOT_KEYWORDS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY1_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY2_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY3_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY4_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY5_TOPICS")

        );

        final User user = SecureCAS.getUser();
        injectWebIdentification(user);
        //推荐时记录cookie
        injectPromoterCookier(user);

        Http.Cookie cookie = request.cookies.get("identity");
        final String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);

        // FIXME: 因为购物车经常出现不同步，先不缓存；但总要使用缓存
        /* CacheHelper.getCache(Cart.getCartCacheKey(user, cookieValue), new CacheCallBack<List<Cart>>() {
           @Override
           public List<Cart> loadData() {
               return Cart.findAll(user, cookieValue);
           }
       }); */

        int count = 0;
        for (Cart cart : carts) {
            count += cart.number;
        }
        //购物车
        renderArgs.put("carts", carts);
        //购物车商品数量
        renderArgs.put("count", count);

        //顶级分类
        List<Category> categories = CacheHelper.getCache(
                CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_TOP_CATEGORIES7"), new CacheCallBack<List<Category>>() {
            @Override
            public List<Category> loadData() {
                return Category.findTop(7);
            }
        });
        renderArgs.put("topCategories", categories);

        //左边顶级分类
        List<Category> leftCategories = CacheHelper.getCache(
                CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_TOP_LEFT_CATEGORIES5"), new CacheCallBack<List<Category>>() {
            @Override
            public List<Category> loadData() {
                return Category.findLeftTop(5);
            }
        });
        renderArgs.put("leftCategories", leftCategories);

        //前n个商圈
        List<Area> areas = CacheHelper.getCache(CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_AREAS6_" + Area.SHANGHAI), new CacheCallBack<List<Area>>() {
            @Override
            public List<Area> loadData() {
                return Area.findTopAreas(6, Area.SHANGHAI);
            }
        });
        renderArgs.put("areas", areas);

        final Date currentDate = new Date();
        //搜索框下面的热门搜索词
        List<Block> hotKeywords = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_TOP_HOT_KEYWORDS"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.HOT_KEYWORDS, currentDate);
            }
        });

        //首页分类的公告1
        Topic categoryTopic1 = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY1_TOPICS"), new CacheCallBack<Topic>() {
            @Override
            public Topic loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.WEB_CATEGORY1, currentDate, 1).get(0);
            }
        });
        //首页分类的公告2
        Topic categoryTopic2 = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY2_TOPICS"), new CacheCallBack<Topic>() {
            @Override
            public Topic loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.WEB_CATEGORY2, currentDate, 1).get(0);
            }
        });
        //首页分类的公告3
        Topic categoryTopic3 = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY3_TOPICS"), new CacheCallBack<Topic>() {
            @Override
            public Topic loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.WEB_CATEGORY3, currentDate, 1).get(0);
            }
        });
        //首页分类的公告4
        Topic categoryTopic4 = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY4_TOPICS"), new CacheCallBack<Topic>() {
            @Override
            public Topic loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.WEB_CATEGORY4, currentDate, 1).get(0);
            }
        });
        //首页分类的公告5
        Topic categoryTopic5 = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY5_TOPICS"), new CacheCallBack<Topic>() {
            @Override
            public Topic loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.WEB_CATEGORY5, currentDate, 1).get(0);
            }
        });
        renderArgs.put("hotKeywords", hotKeywords);
        renderArgs.put("categoryTopic1", categoryTopic1);
        renderArgs.put("categoryTopic2", categoryTopic2);
        renderArgs.put("categoryTopic3", categoryTopic3);
        renderArgs.put("categoryTopic4", categoryTopic4);
        renderArgs.put("categoryTopic5", categoryTopic5);
    }

    private static void injectPromoterCookier(User user) {
        Http.Cookie cookie = request.cookies.get(PROMOTER_COOKIE);
        if (cookie == null) {
            String referCode = request.params.get("tj");
            if (StringUtils.isNotBlank(referCode)) {
                response.setCookie(PROMOTER_COOKIE, referCode, "1d");
            }
            //判断是否通过注册产生的推荐
            if (user != null && user.promoteUserId != null) {
                User promoteUser = User.findById(user.promoteUserId);
                Order order = Order.find("consumerId=?", user.id).first();
                //是推荐的并且第一次下单 购买，才写入cookie
                if (promoteUser != null && order == null)
                    response.setCookie(PROMOTER_COOKIE, promoteUser.promoterCode);
            }
        }
    }

    protected static void injectWebIdentification(final User user) {
        String cookieValue = null;

        cookieValue = getWebIdentificationCookieId();

        final String identificationValue = cookieValue;
        final Long userId = (user != null) ? user.getId() : 0l;

        UserWebIdentification identification = CacheHelper.getCache("WEBIDENTI_" + identificationValue + "_" + userId, new CacheCallBack<UserWebIdentification>() {
            @Override
            public UserWebIdentification loadData() {
                return findUserWebIdentification(user, identificationValue);
            }
        });
        if (identification == null) {
            // 第一次请求时，也设置一下个这对象
            identification = createUserWebIdentification(user, identificationValue);
        }
        _userWebIdentification.set(identification);
    }

    public static String getWebIdentificationCookieId() {
        String cookieValue;
        Http.Cookie cookie = request.cookies.get(WEB_TRACK_COOKIE);

        if (cookie == null) {
            cookieValue = UUID.randomUUID().toString();
            response.setCookie(WEB_TRACK_COOKIE, cookieValue, (Play.mode == Play.Mode.DEV) ? request.domain : baseDomain, "/", -1, false);
        } else {
            cookieValue = cookie.value;
        }
        return cookieValue;
    }

    public static UserWebIdentification getUserWebIdentification() {
        return _userWebIdentification.get();
    }

    @After
    public static void cleanWebIdentification() {
        _userWebIdentification.set(null);
    }

    /**
     * 基于用户信息和cookie值创建或更新userWebIdentification
     *
     * @param user
     * @param identificationValue
     * @return
     */
    private static UserWebIdentification findUserWebIdentification(
            final User user, final String identificationValue) {
        UserWebIdentification uwi = UserWebIdentification.findOne(identificationValue);

        if (uwi == null) {
            // 为避免大量爬虫产生的记录，这里：
            // 如果没有保存过，尝试从Cache找一下，如果找到，让mq可以进行保存操作。        	
            uwi = (UserWebIdentification) Cache.get(UserWebIdentification.MQ_KEY + identificationValue);
            if (uwi != null) {
                uwi.notifyMQSave();
            }
        } else {
            uwi.user = user;
            uwi.save();
        }

        if (uwi == null) {
            // 第一次产生标识对象
            uwi = createUserWebIdentification(user, identificationValue);
            uwi.sendToCacheOrSave();
            if (!StringUtils.isEmpty(uwi.referer)) {
                uwi.notifyMQSave();
            }
            return null; //避免缓存
        }
        return uwi;
    }

    private static UserWebIdentification createUserWebIdentification(
            final User user, final String identificationValue) {
        UserWebIdentification uwi;
        uwi = new UserWebIdentification();
        uwi.cookieId = identificationValue;
        uwi.user = user;
        uwi.firstPage = request.host + request.url;
        uwi.createdAt = new Date();
        uwi.ip = request.remoteAddress;
        uwi.referCode = request.params.get("tj");  //使用tj参数得到推荐码.
        Header header = request.headers.get("referer");
        if (header != null) {
            uwi.referer = header.value();
            if (uwi.referer != null) {
                uwi.refererHost = matchTheHostName(uwi.referer);
            }
        }
        Header headerAgent = request.headers.get("user-agent");
        if (headerAgent != null) {
            uwi.userAgent = headerAgent.value();
        }
        return uwi;
    }

    /**
     * 从URL中匹配出主机名.
     *
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
