package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.PlatformType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.accounts.AccountType;
import models.cms.Block;
import models.cms.BlockType;
import models.cms.FriendsLink;
import models.cms.Topic;
import models.cms.TopicType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
import models.sales.BrowsedGoods;
import models.sales.Category;
import org.apache.commons.collections.CollectionUtils;
import play.mvc.After;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 首页控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 9:57 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Home2 extends Controller {

    public static void index(final long categoryId) {
        User user = SecureCAS.getUser();

        CacheHelper.preRead(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_NEW4"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_RECOMMENDS4"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_HOT_SALE4"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_GOODS"),
                CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_FLOOR_CATEGORIES"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_TOPICS"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_RIGHT_SLIDES"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SLIDES"),
                CacheHelper.getCacheKey(FriendsLink.CACHEKEY, "FRIENDS_LINK"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_1F"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_2F"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY1_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY2_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY3_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY4_TOPICS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "CATEGORY5_TOPICS")
        );

        //最新上架，新品推荐
        List<models.sales.Goods> newGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_NEW4"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findNewGoods(4);
            }
        });

        //猜你喜欢
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_RECOMMENDS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                List<BrowsedGoods> browsedGoodsList = BrowsedGoods.findTop(4, 2);
                List<models.sales.Goods> goodsList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(browsedGoodsList)) {
                    for (BrowsedGoods browsedGoods : browsedGoodsList) {
                        goodsList.add(browsedGoods.goods);
                    }
                }
                return goodsList;
            }
        });

        //推荐商品
        List<models.sales.Goods> goodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_GOODS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return getTopGoods(categoryId);
            }
        });
        //热卖商品，销量最多的商品
        List<models.sales.Goods> hotSaleGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_HOT_SALE4"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findTopHotSale(4);
            }
        });

        //楼层顶级分类
        List<Category> floorCategories = CacheHelper.getCache(CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_FLOOR_CATEGORIES"), new CacheCallBack<List<Category>>() {
            @Override
            public List<Category> loadData() {
                return Category.findFloorTop(5);
            }
        });

        renderArgs.put("categoryId", categoryId);

        final Date currentDate = new Date();

        //公告
        List<Topic> topics = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_TOPICS"), new CacheCallBack<List<Topic>>() {
            @Override
            public List<Topic> loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.TOPIC, currentDate, 4);
            }
        });
        //首屏小图图片展示
        List<Block> rightSlides = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_RIGHT_SLIDES"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.WEBSITE_RIGHT_SLIDE, currentDate);
            }
        });

        // CMS 区块
        List<Block> slides = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SLIDES"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.WEBSITE_SLIDE, currentDate);
            }
        });

        //友情链接
        List<FriendsLink> friendsLinks = CacheHelper.getCache(CacheHelper.getCacheKey(FriendsLink.CACHEKEY, "FRIENDS_LINK"), new CacheCallBack<List<FriendsLink>>() {
            @Override
            public List<FriendsLink> loadData() {
                return FriendsLink.findAllByDeleted();
            }
        });
        renderArgs.put("friendsLinks", friendsLinks);

        //首页楼层banner
        Block webOneFloor = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_1F"), new CacheCallBack<Block>() {
            @Override
            public Block loadData() {
                return Block.findByType(BlockType.WEBSITE_1F, currentDate).get(0);
            }
        });
        //首页楼层banner
        Block webTwoFloor = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_2F"), new CacheCallBack<Block>() {
            @Override
            public Block loadData() {
                return Block.findByType(BlockType.WEBSITE_2F, currentDate).get(0);
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
        //首页分类的公告1
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

        if (user != null) {
            //待消费
            long unconsumedCount = ECoupon.count("e.order.userId = ? and e.order.userType = ? and status = ?",
                    user.getId(), AccountType.CONSUMER, ECouponStatus.UNCONSUMED);
            //待付款
            long unpaidCount = Order.count("byUserIdAndUserTypeAndStatus",
                    user.getId(), AccountType.CONSUMER, OrderStatus.UNPAID);
            //已节省
            BigDecimal savedMoney = ECoupon.savedMoney(user.getId(), AccountType.CONSUMER);

            renderArgs.put("unconsumedCount", unconsumedCount);
            renderArgs.put("unpaidCount", unpaidCount);
            renderArgs.put("savedMoney", savedMoney);
        }

        renderArgs.put("categoryTopic1", categoryTopic1);
        renderArgs.put("categoryTopic2", categoryTopic2);
        renderArgs.put("categoryTopic3", categoryTopic3);
        renderArgs.put("categoryTopic4", categoryTopic4);
        renderArgs.put("categoryTopic5", categoryTopic5);
        renderArgs.put("webOneFloor", webOneFloor);
        renderArgs.put("webTwoFloor", webTwoFloor);
        renderArgs.put("categoryId", categoryId);
        renderArgs.put("friendsLinks", friendsLinks);
        renderArgs.put("slides", slides);
        renderArgs.put("rightSlides", rightSlides);
        renderArgs.put("isHome", Boolean.TRUE);
        render(goodsList, newGoodsList, recommendGoodsList, hotSaleGoodsList, floorCategories, topics);
    }

    private static List<models.sales.Goods> getTopGoods(long categoryId) {
        List<models.sales.Goods> goodsList;
        if (categoryId == 0) {
            goodsList = models.sales.Goods.findTop(4);
        } else {
            goodsList = models.sales.Goods.findTopByCategory(categoryId, 4);
        }
        return goodsList;
    }

    @After
    public static void clearCache() {
        CacheHelper.cleanPreRead();
    }
}
