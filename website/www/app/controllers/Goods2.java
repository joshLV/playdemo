package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.Block;
import models.cms.BlockType;
import models.cms.CmsQuestion;
import models.cms.GoodsType;
import models.consumer.User;
import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.BrowsedGoods;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.GoodsStatistics;
import models.sales.GoodsStatisticsType;
import models.sales.GoodsWebsiteCondition;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import play.modules.breadcrumbs.Breadcrumb;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.SimplePaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.After;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 商品控制器.
 * <p/>
 * User: sujie
 * Date: 10/25/12
 * Time: 2:53 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Goods2 extends Controller {

    public static int PAGE_SIZE = 27;

    /**
     * 商品列表初始页
     * 关键字搜索后进入此页.
     */
    public static void index() {
        String page = params.get("page");
        String keywords = params.get("s");
        renderArgs.put("s", keywords);

        String brandIdStr = params.get("b");
        renderArgs.put("b", brandIdStr);
        long brandId = StringUtils.isBlank(brandIdStr) ? 0 : Long.parseLong(brandIdStr);

        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        CacheHelper.preRead(CacheHelper.getCacheKey(new String[]{Goods.CACHEKEY, Goods.CACHEKEY_BASEID, GoodsStatistics.CACHEKEY},
                "SHOW_TOP" + PAGE_SIZE + "RECOMMEND"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_RIGHT_SLIDES"),
                CacheHelper.getCacheKey(Goods.CACHEKEY, "WWW_HOT_SALE4"));

        //只按关键字搜索
        QueryResponse queryResponse = models.sales.Goods.search(keywords, brandId, pageNumber, PAGE_SIZE);

        //搜索结果的全部商品数量
        Long onSaleGoodsCount = queryResponse.getResults().getNumFound();
        if (onSaleGoodsCount != null && onSaleGoodsCount > 0) {
            //搜索结果的商品页
            SimplePaginator<Goods> goodsPage = Goods.getResultPage(queryResponse, pageNumber, PAGE_SIZE);
            renderArgs.put("goodsPage", goodsPage);

            //搜索结果的区域
            List<Area> districts = Goods.getStatisticDistricts(queryResponse);
            renderArgs.put("districts", districts);
            //搜索结果的分类
            List<Category> searchCategories = Goods.getStatisticTopCategories(queryResponse);
            renderArgs.put("searchCategories", searchCategories);
        } else {
            // 网友推荐商品
            renderRecommendGoods(PAGE_SIZE);
        }
        //面包屑导航
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        if (StringUtils.isNotBlank(keywords)) {
            breadcrumbs.append("搜索结果", "");
        }

        //输出右侧展示条数据
        renderRightBar(null);

        final GoodsWebsiteCondition condition = new GoodsWebsiteCondition("", keywords, brandId);

        render(breadcrumbs, onSaleGoodsCount, condition);
    }


    /**
     * 商品列表按条件查询页.
     *
     * @param conditionStr 查询条件
     */
    public static void search(String conditionStr) {
        String page = params.get("page");
        String keywords = params.get("s");
        renderArgs.put("s", keywords);
        String brandIdStr = params.get("b");
        renderArgs.put("b", brandIdStr);
        long brandId = StringUtils.isBlank(brandIdStr) ? 0 : Long.parseLong(brandIdStr);

        final int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer
                .parseInt(page);

        CacheHelper.preRead(CacheHelper.getCacheKey(new String[]{Goods.CACHEKEY, Goods.CACHEKEY_BASEID, GoodsStatistics.CACHEKEY},
                "SHOW_TOP" + PAGE_SIZE + "RECOMMEND"),
                CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_RIGHT_SLIDES"),
                CacheHelper.getCacheKey(Goods.CACHEKEY, "WWW_HOT_SALE4"));

        try {
            final GoodsWebsiteCondition condition = new GoodsWebsiteCondition(conditionStr, keywords, brandId);

            //获取要显示的子分类列表
            Category currentCategory = null;
            if (condition.categoryId > 0) {
                currentCategory = Category.findCategoryById(condition.categoryId);
                if (currentCategory != null && currentCategory.isRoot()) { //如果指定为顶级分类
                    condition.parentCategoryId = condition.categoryId;
                    condition.categoryId = 0;
                }
            }

            //获取搜索到的商品
            QueryResponse queryResponse = models.sales.Goods.searchFullText(condition, pageNumber, PAGE_SIZE);
            SimplePaginator<Goods> goodsPage = models.sales.Goods.getResultPage(queryResponse, pageNumber, PAGE_SIZE);

            //搜索结果的全部商品数量
            Long onSaleGoodsCount = queryResponse.getResults().getNumFound();

            if (onSaleGoodsCount != null && onSaleGoodsCount > 0) {
                //搜索结果的分类统计
                List<Category> searchCategories = models.sales.Goods.getStatisticTopCategories(queryResponse);
                renderArgs.put("searchCategories", searchCategories);

                if (condition.parentCategoryId > 0) { //如果指定了顶级分类，则统计子分类
                    List<Category> subCategories = Goods.getStatisticSubCategories(queryResponse, condition.parentCategoryId);
                    renderArgs.put("subCategories", subCategories);
                } else if (condition.categoryId > 0) {
                    List<Category> subCategories = Goods.getStatisticSubCategories(queryResponse, currentCategory.parentCategory.id);
                    renderArgs.put("subCategories", subCategories);
                }

                //搜索结果的地区统计
                List<Area> districts = Goods.getStatisticDistricts(queryResponse);
                renderArgs.put("districts", districts);

                if (!"0".equals(condition.districtId)) {
                    List<Area> searchAreas = models.sales.Goods.getStatisticAreas(queryResponse, condition.districtId);
                    renderArgs.put("searchAreas", searchAreas);
                } else if (!"0".equals(condition.searchAreaId)) {
                    List<Area> searchAreas = models.sales.Goods.getStatisticAreas(queryResponse, Area.findAreaById(condition.searchAreaId).parent.id);
                    renderArgs.put("searchAreas", searchAreas);
                }
            } else {
                renderRecommendGoods(PAGE_SIZE);

            }

            BreadcrumbList breadcrumbs = createBreadcrumbs(condition);

            renderArgs.put("categoryId", (condition.parentCategoryId != 0) ? condition.parentCategoryId : condition.categoryId);

            //输出右侧展示条数据
            renderRightBar(null);

            renderGoodsListTitle(condition);
            render("/Goods2/index.html", onSaleGoodsCount, goodsPage, breadcrumbs, condition);
        } catch (Exception e) {
            e.printStackTrace();
            index();
        }
    }

    private static void renderRecommendGoods(final int count) {
        // 网友推荐商品
        List<Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[]{Goods.CACHEKEY, Goods.CACHEKEY_BASEID, GoodsStatistics.CACHEKEY},
                        "SHOW_TOP" + count + "RECOMMEND"),
                new CacheCallBack<List<Goods>>() {
                    @Override
                    public List<Goods> loadData() {
                        return Goods.findTopRecommend(count);
                    }
                });
        renderArgs.put("recommendGoodsList", recommendGoodsList);
    }

    /**
     * 商品详情.
     *
     * @param id 商品
     */
    public static void show(final long id) {
        final Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();
        CacheHelper.preRead(CacheHelper.getCacheKey(
                models.sales.Goods.CACHEKEY_BASEID + id, "UNDELETED"),
                CacheHelper.getCacheKey(
                        models.sales.Goods.CACHEKEY_BASEID + id, "KEYWORDMAP"),
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "BREADCRUMBS"),
                CacheHelper.getCacheKey(new String[]{Order.CACHEKEY_BASEUSERID + userId,
                        models.sales.Goods.CACHEKEY_BASEID + id}, "LIMITNUMBER"));
        final models.sales.Goods goods = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "UNDELETED"), new CacheCallBack<models.sales.Goods>() {
            @Override
            public models.sales.Goods loadData() {
                return models.sales.Goods.findUnDeletedById(id);
            }
        });

        if (goods == null) {
            error(404, "没有找到该商品！");
        }
        showGoods(goods);
        render();
    }

    public static void preview(String uuid, boolean isSupplier) {
        models.sales.Goods goods = models.sales.Goods.getPreviewGoods(uuid);
        if (goods == null) {
            error(404, "没有找到该商品！");
        }
        showGoods(goods);
        render("Goods2/show.html", isSupplier);
    }

    private static void showGoods(final models.sales.Goods goods) {
        if (goods == null) {
            notFound();
        }
        // 登陆的场合，判断该会员是否已经购买过此限购商品
        final User user = SecureCAS.getUser();
        //该用户曾经购买该商品的数量
        Long boughtNumber = 0l;
        int addCartNumber = 0;
        if (user != null) {
            //取得已经加入购物车的数量
            addCartNumber = Cart.findAllByGoodsId(user, goods);
            boughtNumber = OrderItems.itemsNumber(user, goods.id);

            final Long finalBoughtNumber = boughtNumber;
            Boolean isBuyFlag = CacheHelper.getCache(CacheHelper.getCacheKey(
                    new String[]{Order.CACHEKEY_BASEUSERID + user.id,
                            models.sales.Goods.CACHEKEY_BASEID + goods.id},
                    "LIMITNUMBER"), new CacheCallBack<Boolean>() {
                @Override
                public Boolean loadData() {
                    return Order.checkLimitNumber(user, goods.id, finalBoughtNumber, 1);
                }
            });
            renderArgs.put("user", user);
            renderArgs.put("addCartNumber", addCartNumber);
            renderArgs.put("bought", isBuyFlag);
        }
        renderArgs.put("boughtNumber", boughtNumber);
        BreadcrumbList breadcrumbs = CacheHelper.getCache(
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY_BASEID
                        + goods.id, "BREADCRUMBS"),
                new CacheCallBack<BreadcrumbList>() {
                    @Override
                    public BreadcrumbList loadData() {
                        return getGoodsBreadCrumbs(goods.id);
                    }
                });
        //输出右侧展示条数据
        renderRightBar(goods);

        //统计浏览的数量
        GoodsStatistics.addVisitorCount(goods.id);

        //记录用户浏览过的商品
        Http.Cookie idCookie = request.cookies.get("identity");
        final String cookieValue = idCookie == null ? null : idCookie.value;
        BrowsedGoods.addVisitorCount(user, cookieValue, goods);

        //查询浏览过的商品
        List<BrowsedGoods> browsedGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[]{models.sales.Goods.CACHEKEY,
                        models.sales.Goods.CACHEKEY_BASEID + goods.id},
                        "BROWSED_GOODS"),
                new CacheCallBack<List<BrowsedGoods>>() {
                    @Override
                    public List<BrowsedGoods> loadData() {
                        return BrowsedGoods.find(user, cookieValue, 5);
                    }
                });

        String tjUrl = "http://www." + play.Play.configuration.getProperty("application.baseDomain") + "/p/" + goods.id;
        if (user != null) {
            user.generatePromoterCode();
            tjUrl += "?tj=" + user.promoterCode;
        } else {
            tjUrl += "?tj=gshare";
        }

        renderArgs.put("tjUrl", tjUrl);
        renderArgs.put("browsedGoodsList", browsedGoodsList);
        renderArgs.put("goods", goods);
        renderArgs.put("imagesList", goods.getCachedGoodsImagesList());
        renderArgs.put("breadcrumbs", breadcrumbs);
    }

    private static List<Block> renderRightBar(final Goods goods) {
        final Date currentDate = new Date();

        //右上侧图片展示
        List<Block> rightSlides = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_RIGHT_SLIDES"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.WEBSITE_RIGHT_SLIDE, currentDate);
            }
        });
        renderArgs.put("rightSlides", rightSlides);

        //热卖商品，销量最多的商品
        List<Goods> hotSaleGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(Goods.CACHEKEY, "WWW_HOT_SALE4"), new CacheCallBack<List<Goods>>() {
            @Override
            public List<Goods> loadData() {
                return Goods.findTopHotSale(4);
            }
        });
        renderArgs.put("hotSaleGoodsList", hotSaleGoodsList);
        if (goods != null) {
            renderRecommendGoods(goods);


        } else {
            //感兴趣的商品
            renderRecommendGoods(5);
        }

        return rightSlides;
    }

    private static void renderRecommendGoods(final Goods goods) {
        //感兴趣的商品
        List<Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[]{Goods.CACHEKEY,
                        Goods.CACHEKEY_BASEID + goods.id},
                        "SHOW_TOP5RECOMMEND"),
                new CacheCallBack<List<Goods>>() {
                    @Override
                    public List<Goods> loadData() {
                        return Goods.findTopRecommendByGoods(5, goods);
                    }
                });
        renderArgs.put("recommendGoodsList", recommendGoodsList);
    }

    /**
     * 获取商户的门店信息
     *
     * @param id
     */
    public static void shops(Long id) {
        String currPage = params.get("currPage");
        int pageNumber = StringUtils.isEmpty(currPage) ? 1 : Integer.parseInt(currPage);
        String pageSize1 = params.get("pageSize");
        int pageSize = StringUtils.isEmpty(pageSize1) ? 1 : Integer.parseInt(pageSize1);
        final Long goodsId = id;
        final models.sales.Goods goods = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "UNDELETED"), new CacheCallBack<models.sales.Goods>() {
            @Override
            public models.sales.Goods loadData() {
                return models.sales.Goods.findUnDeletedById(goodsId);
            }
        });

        ValuePaginator<Shop> shops = new ValuePaginator<>(
                goods.getShopList());
        shops.setPageNumber(pageNumber);
        shops.setPageSize(pageSize);

        render("Goods2/shops.json", shops, pageNumber, pageSize);
    }

    private static void showGoodsHistory(final GoodsHistory goodsHistory) {
        if (goodsHistory == null) {
            notFound();
        }
        // 登陆的场合，判断该会员是否已经购买过此限购商品
        final User user = SecureCAS.getUser();
        //该用户曾经购买该商品的数量
        Long boughtNumber = 0l;
        int addCartNumber = 0;
        if (user != null) {
            //取得已经加入购物车的数量
            final models.sales.Goods goods = models.sales.Goods.findById(goodsHistory.goodsId);
            addCartNumber = Cart.findAllByGoodsId(user, goods);
            boughtNumber = OrderItems.itemsNumber(user, goods.id);

            final Long finalBoughtNumber = boughtNumber;
            Boolean isBuyFlag = CacheHelper.getCache(CacheHelper.getCacheKey(
                    new String[]{Order.CACHEKEY_BASEUSERID + user.id,
                            models.sales.Goods.CACHEKEY_BASEID + goods.id},
                    "LIMITNUMBER"), new CacheCallBack<Boolean>() {
                @Override
                public Boolean loadData() {
                    return Order.checkLimitNumber(user, goods.id, finalBoughtNumber, 1);
                }
            });
            renderArgs.put("user", user);
            renderArgs.put("addCartNumber", addCartNumber);
            renderArgs.put("bought", isBuyFlag);
        }
        renderArgs.put("boughtNumber", boughtNumber);

        final models.sales.Goods goods = models.sales.Goods.findById(goodsHistory.goodsId);

        BreadcrumbList breadcrumbs = CacheHelper.getCache(
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY_BASEID
                        + goods.id, "BREADCRUMBS"),
                new CacheCallBack<BreadcrumbList>() {
                    @Override
                    public BreadcrumbList loadData() {
                        return getGoodsBreadCrumbs(goods.id);
                    }
                });

        // 网友推荐商品
        renderRecommendGoods(goods);
        String tjUrl = "http://www." + play.Play.configuration.getProperty("application.baseDomain") + "/gh/" + goodsHistory.id;
        if (user != null) {
            user.generatePromoterCode();
            tjUrl += "?tj=" + user.promoterCode;
        } else {
            tjUrl += "?tj=gshare";
        }

        renderArgs.put("tjUrl", tjUrl);
        renderArgs.put("goods", goodsHistory);
        renderArgs.put("shops", goodsHistory.getShopList());
        renderArgs.put("breadcrumbs", breadcrumbs);
    }


    private static BreadcrumbList getGoodsBreadCrumbs(long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        long categoryId = 0;
        if (goods.categories != null && goods.categories.size() > 0) {
            Category category = goods.categories.iterator().next();
            categoryId = category.id;
            breadcrumbs.append(category.name, "/q/" + category.id);
        }
        if (goods.brand != null) {
            breadcrumbs.append(goods.brand.name, "/q/" + categoryId + "-021-"
                    + goods.brand.id);
        }
        return breadcrumbs;
    }

    /**
     * 获取用户的咨询信息
     *
     * @param id
     */
    public static void questions(Long id) {

        final Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();
        Http.Cookie idCookie = request.cookies.get("identity");
        final String cookieValue = idCookie == null ? null : idCookie.value;

        CacheHelper.preRead(CacheHelper.getCacheKey(
                models.sales.Goods.CACHEKEY_BASEID + id, "QUESTION_u"
                + userId + "_c" + cookieValue));
        String currPage = params.get("currPage");
        int pageNumber = StringUtils.isEmpty(currPage) ? 1 : Integer.parseInt(currPage);
        String pageSize1 = params.get("pageSize");
        int pageSize = StringUtils.isEmpty(pageSize1) ? 1 : Integer.parseInt(pageSize1);
        final Long goodsId = id;
        List<CmsQuestion> questions = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "QUESTION_u" + userId + "_c" + cookieValue),
                new CacheCallBack<List<CmsQuestion>>() {
                    @Override
                    public List<CmsQuestion> loadData() {
                        return CmsQuestion.findOnGoodsShow(userId, cookieValue,
                                goodsId, GoodsType.NORMALGOODS, 0, 999);
                    }
                });
        ValuePaginator<CmsQuestion> cmsQuestions = new ValuePaginator<>(questions);
        cmsQuestions.setPageNumber(pageNumber);
        cmsQuestions.setPageSize(pageSize);

        render("Goods2/questions.json", cmsQuestions, pageNumber, pageSize);
    }

    /**
     * 商品历史详情.
     *
     * @param id 商品
     */
    public static void showHistory(final long id) {
        final Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();
        GoodsHistory goodsHistory = GoodsHistory.findById(id);
        if (goodsHistory == null) {
            error(404, "没有找到该商品！");
        }
        showGoodsHistory(goodsHistory);
        List<CmsQuestion> questions = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "QUESTION_u" + userId + "_c" + null),
                new CacheCallBack<List<CmsQuestion>>() {
                    @Override
                    public List<CmsQuestion> loadData() {
                        return CmsQuestion.findOnGoodsShow(userId, null,
                                id, GoodsType.NORMALGOODS, 0, 10);
                    }
                });
        renderArgs.put("questions", questions);
        render("Goods2/show.html");
    }


    /**
     * 根据用户的选择，统计商品的喜欢及其他指数.
     *
     * @param id 商品标识
     */
    public static void statistics(long id, GoodsStatisticsType statisticsType) {
        if (statisticsType == GoodsStatisticsType.LIKE) {
            GoodsStatistics.addLikeCount(id);
        } else if (statisticsType == GoodsStatisticsType.ADD_CART) {
            GoodsStatistics.addCartCount(id);
        } else if (statisticsType == GoodsStatisticsType.BUY) {
            GoodsStatistics.addBuyCount(id);
        } else if (statisticsType == GoodsStatisticsType.VISITOR) {
            GoodsStatistics.addVisitorCount(id);
        }
        final Long goodsId = id;
        GoodsStatistics statistics = CacheHelper.getCache(CacheHelper.getCacheKey(GoodsStatistics.CACHEKEY_GOODSID + goodsId, "GOODSSTATS"), new CacheCallBack<GoodsStatistics>() {
            @Override
            public GoodsStatistics loadData() {
                return GoodsStatistics.find("goodsId", goodsId).first();
            }
        });
        renderJSON(statistics == null ? "0" : statistics.summaryCount.toString());
    }

    /**
     * 清理预读缓存.
     */
    @After
    public static void clearCache() {
        CacheHelper.cleanPreRead();
    }

    private static void renderGoodsListTitle(GoodsWebsiteCondition goodsCond) {
        List<String> titleList = new ArrayList<>();
        if (goodsCond.categoryId > 0) {
            Category c = Category.findCategoryById(goodsCond.categoryId);
            titleList.add(c.name);
            if (StringUtils.isNotBlank(c.keywords)) {
                titleList.add(c.keywords);
            }
            if (c.parentCategory != null) {
                renderArgs.put("categoryId", c.parentCategory.id);
            }
        }
        if (!"0".equals(goodsCond.cityId)) {
            Area city = Area.findById(goodsCond.cityId);
            titleList.add(city.name);
        }
        if (!"0".equals(goodsCond.districtId)) {
            Area district = Area.findById(goodsCond.districtId);
            titleList.add(district.name);
        }
        if (!"0".equals(goodsCond.areaId)) {
            Area area = Area.findById(goodsCond.areaId);
            titleList.add(area.name);
        }

        if (titleList.size() > 0) {
            renderArgs.put("condsTitle", StringUtils.join(titleList, " "));
        } else {
            renderArgs.put("condsTitle", "所有商品");
        }
    }

    private static BreadcrumbList createBreadcrumbs(GoodsWebsiteCondition goodsCond) {
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        if (goodsCond == null || goodsCond.isDefault() && StringUtils.isBlank(goodsCond.keywords)) {
            return breadcrumbs;
        }
        final Breadcrumb categoryCrumb = createCategoryCrumb(goodsCond);
        if (GoodsWebsiteCondition.isValidAreaId(goodsCond.areaId)) {
            breadcrumbs.add(createTopCategoryCrumb(goodsCond));
            if (categoryCrumb != null) {
                breadcrumbs.add(categoryCrumb);
            }
            breadcrumbs.add(createDistrictCrumb(goodsCond));
            breadcrumbs.add(createAreaCrumb(goodsCond));
        } else if (GoodsWebsiteCondition.isValidAreaId(goodsCond.districtId)) {
            breadcrumbs.add(createTopCategoryCrumb(goodsCond));
            if (categoryCrumb != null) {
                breadcrumbs.add(categoryCrumb);
            }
            breadcrumbs.add(createDistrictCrumb(goodsCond));
        } else if (goodsCond.categoryId > 0) {
            breadcrumbs.add(createTopCategoryCrumb(goodsCond));
            if (categoryCrumb != null) {
                breadcrumbs.add(categoryCrumb);
            }
        } else if (goodsCond.parentCategoryId > 0) {
            breadcrumbs.add(createTopCategoryCrumb(goodsCond));
        }
        return breadcrumbs;
    }

    private static Breadcrumb createTopCategoryCrumb(GoodsWebsiteCondition goodsCond) {
        String url = goodsCond.buildUrl("parentCategoryId", goodsCond.parentCategoryId).getUrl();
        String desc;
        if (goodsCond.parentCategoryId > 0) {
            Category category = Category.findCategoryById(goodsCond.parentCategoryId);
            desc = category.name;
        } else {
            desc = "全部分类";
        }
        return new Breadcrumb(desc, url);
    }

    private static Breadcrumb createCategoryCrumb(GoodsWebsiteCondition goodsCond) {
        String url = goodsCond.buildUrl("categoryId", goodsCond.categoryId).getUrl();
        String desc;
        if (goodsCond.categoryId > 0) {
            Category category = Category.findCategoryById(goodsCond.categoryId);
            desc = category.name;
            return new Breadcrumb(desc, url);
        }
        return null;
    }

    private static Breadcrumb createAreaCrumb(GoodsWebsiteCondition goodsCond) {
        String url = goodsCond.buildUrl("areaId", goodsCond.areaId).getUrl();
        String desc;
        if (GoodsWebsiteCondition.isValidAreaId(goodsCond.areaId)) {
            Area area = Area.findById(goodsCond.areaId);
            desc = area.name;
        } else {
            desc = "全部商圈";
        }
        return new Breadcrumb(desc, url);
    }

    private static Breadcrumb createDistrictCrumb(GoodsWebsiteCondition goodsCond) {
        String url = goodsCond.buildUrl("districtId", goodsCond.districtId).getUrl();
        String desc;
        if (GoodsWebsiteCondition.isValidAreaId(goodsCond.districtId)) {
            Area district = Area.findById(goodsCond.districtId);
            desc = district.name;
        } else {
            desc = "全部地区";
        }
        return new Breadcrumb(desc, url);
    }

}
