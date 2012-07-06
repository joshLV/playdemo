package controllers;

import java.util.Date;
import java.util.List;

import models.cms.Block;
import models.cms.BlockType;
import models.cms.Topic;
import models.cms.TopicType;
import models.order.Order;
import models.sales.Area;
import models.sales.Category;
import play.Logger;
import play.mvc.After;
import play.mvc.Controller;
import play.mvc.With;
import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.PlatformType;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;

/**
 * 首页控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 9:57 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Home extends Controller {

    public static void index(final long categoryId) {
        CacheHelper.preRead(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_TOPS"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_RECENTS"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_RECOMMENDS"),
                CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_DISTRICTS"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SAILY_SPEC"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SLIDES"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_RIGHT_SLIDES"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_NEWS"),
                CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_TOPICS"),
                CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_TOPCATEGORIES"),
                CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_AREAS"),
                CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_NEW"),
                CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_SUPPLIER")

        );

        //精选商品        
        List<models.sales.Goods> goodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_TOPS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return getTopGoods(categoryId);
            }
        });

        //近日成交商品
        List<models.sales.Goods> recentGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(new String[]{models.sales.Goods.CACHEKEY, Order.CACHEKEY}, "WWW_RECENTS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findTradeRecently(5);
            }
        });

        //新品推荐
        List<models.sales.Goods> newGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_NEW"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findNewGoods(8);
            }
        });

        //网友推荐商品
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_RECOMMENDS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findTopRecommend(3);
            }
        });

        //前n个区域
        List<Area> districts = CacheHelper.getCache(CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_DISTRICTS"), new CacheCallBack<List<Area>>() {
            @Override
            public List<Area> loadData() {
                return Area.findTopDistricts(Goods.SHANGHAI, 12);
            }
        });
        //前n个商圈
        List<Area> areas = CacheHelper.getCache(CacheHelper.getCacheKey(Area.CACHEKEY, "WWW_AREAS"), new CacheCallBack<List<Area>>() {
            @Override
            public List<Area> loadData() {
                return Area.findTopAreas(13);
            }
        });

        List<Category> categories = CacheHelper.getCache(CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_TOPCATEGORIES"), new CacheCallBack<List<Category>>() {
            @Override
            public List<Category> loadData() {
                return Category.findTop(8);
            }
        });

        renderArgs.put("categoryId", categoryId);
        final Date currentDate = new Date();

        //最新动态
        List<Topic> newTopics = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_NEWS"), new CacheCallBack<List<Topic>>() {
            @Override
            public List<Topic> loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.NEWS, currentDate, 5);
            }
        });

        //公告
        List<Topic> topics = CacheHelper.getCache(CacheHelper.getCacheKey(Topic.CACHEKEY, "WWW_TOPICS"), new CacheCallBack<List<Topic>>() {
            @Override
            public List<Topic> loadData() {
                return Topic.findByType(PlatformType.UHUILA, TopicType.TOPIC, currentDate, 10);
            }
        });
        //右侧图片展示
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

        List<Block> dailySpecials = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SAILY_SPEC"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.DAILY_SPECIAL, currentDate);
            }
        });

        //合作商家信息
        List<Block> suppliers = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SUPPLIER"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.WEBSITE_SUPPLIER, currentDate);
            }
        });

        models.sales.Goods dailySpecialGoods = null;
        Block dailySpecial = null;
        if (dailySpecials.size() >= 1) {
            dailySpecial = dailySpecials.get(0);
            try {
                final Long goodsId = Long.parseLong(dailySpecial.title);
                dailySpecialGoods = CacheHelper.getCache(models.sales.Goods.CACHEKEY + goodsId, new CacheCallBack<models.sales.Goods>() {
                    @Override
                    public models.sales.Goods loadData() {
                        return models.sales.Goods.findById(goodsId);
                    }
                });
            } catch (Exception e) {
                Logger.warn("每日特卖异常", e);
            }
            if (dailySpecialGoods == null) {
                Logger.info("设置每日特卖商品失败，找不到" + dailySpecial.title + "对应的商品，使用推荐商品");
            }
        }
        if (dailySpecialGoods == null) {
            Logger.info("没有设置每日特卖商品，使用推荐商品");
            if (recommendGoodsList.size() > 0) {
                dailySpecialGoods = recommendGoodsList.get(0);
            }
        }
        renderArgs.put("slides", slides);
        renderArgs.put("newTopics", newTopics);
        renderArgs.put("dailySpecial", dailySpecial);
        renderArgs.put("dailySpecialGoods", dailySpecialGoods);
        renderArgs.put("rightSlides", rightSlides);
        renderArgs.put("suppliers", suppliers);
        render(goodsList, recentGoodsList, newGoodsList, recommendGoodsList, categories, districts, areas, topics);
    }

    @After
    public static void clearCache() {
        CacheHelper.cleanPreRead();
    }

    private static List<models.sales.Goods> getTopGoods(long categoryId) {
        List<models.sales.Goods> goodsList;
        if (categoryId == 0) {
            goodsList = models.sales.Goods.findTop(6);
        } else {
            goodsList = models.sales.Goods.findTopByCategory(categoryId, 6);
        }
        return goodsList;
    }
}
