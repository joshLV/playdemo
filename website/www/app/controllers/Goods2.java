package controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.cms.Block;
import models.cms.BlockType;
import models.cms.CmsQuestion;
import models.cms.GoodsType;
import models.consumer.User;
import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.BrowsedGoods;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsHistory;
import models.sales.GoodsStatistics;
import models.sales.GoodsStatisticsType;
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
import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;

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

    public static String SHANGHAI = "021";
    public static int PAGE_SIZE = 27;

    /**
     * 商品列表初始页
     * 关键字搜索后进入此页.
     */
    public static void index() {
        String page = params.get("page");
        String keywords = params.get("s");
        renderArgs.put("s", keywords);

        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        // 网友推荐商品
        List<models.sales.Goods> recommendGoodsList = models.sales.Goods
                .findTopRecommend(6);

        //只按关键字搜索
        QueryResponse queryResponse = models.sales.Goods.search(keywords, pageNumber, PAGE_SIZE);
        SimplePaginator<Goods> goodsPage = Goods.getResultPage(queryResponse, pageNumber, PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(false);

        //搜索结果的全部商品数量
        Long onSaleGoodsCount = queryResponse.getResults().getNumFound();

        //搜索结果的区域
        List<Area> districts = Goods.getStatisticAreas(queryResponse, true);
        //搜索结果的分类
        List<Category> searchCategories = Goods.getStatisticCategories(queryResponse, true);

        BreadcrumbList breadcrumbs = new BreadcrumbList("所有分类", "/s");
        if (StringUtils.isNotBlank(keywords)) {
            breadcrumbs.append(onSaleGoodsCount + "件商品", "/s?s=" + keywords);
        }

        render(breadcrumbs, recommendGoodsList, onSaleGoodsCount, goodsPage, districts,
                searchCategories);
    }


    /**
     * 商品列表按条件查询页.
     *
     * @param condition 查询条件
     */
    public static void search(String condition) {
        String page = params.get("page");
        String keywords = params.get("s");
        renderArgs.put("s", keywords);

        final int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer
                .parseInt(page);

        try {

            final GoodsCondition goodsCond = new GoodsCondition(condition);

//            goodsCond.status = GoodsStatus.ONSALE;
//            goodsCond.baseSaleBegin = 1;
//            goodsCond.expireAtBegin = new Date();
            //获取要显示的子分类列表

            Category currentCategory = null;
            if (goodsCond.categoryId > 0) {
                currentCategory = Category.findById(goodsCond.categoryId);
            }
            if (currentCategory != null && currentCategory.isRoot()) {
                goodsCond.parentCategoryId = goodsCond.categoryId;
                goodsCond.categoryId = 0;
            }

            // 网友推荐商品
            List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                    CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY,
                            "LIST_TOPRECOMMEND"),
                    new CacheCallBack<List<Goods>>() {
                        @Override
                        public List<models.sales.Goods> loadData() {
                            return models.sales.Goods.findTopRecommend(3);
                        }
                    });

            //获取搜索到的商品
            QueryResponse queryResponse = models.sales.Goods.searchFullText(keywords,
                    goodsCond.parentCategoryId, goodsCond.categoryId, goodsCond.districtId, goodsCond.areaId,
                    goodsCond.solrOrderBy, false, pageNumber, PAGE_SIZE);
            SimplePaginator<Goods> goodsPage = models.sales.Goods.getResultPage(queryResponse, pageNumber, PAGE_SIZE);
            goodsPage.setBoundaryControlsEnabled(false);

            //搜索结果的全部商品数量
            Long onSaleGoodsCount = queryResponse.getResults().getNumFound();

            List<Category> searchCategories = models.sales.Goods.getStatisticCategories(queryResponse, true);
            List<Category> subCategories = currentCategory == null ? null : Goods.getStatisticCategories(queryResponse, false);

            //搜索结果的区
            List<Area> districts = Goods.getStatisticAreas(queryResponse, true);
            List<Area> searchAreas = models.sales.Goods.getStatisticAreas(queryResponse, false);

            /*List<Area> areas = CacheHelper.getCache(
                    CacheHelper.getCacheKey(Area.CACHEKEY, "LIST_AREAS"
                            + goodsCond.districtId + "_" + goodsCond.areaId
                            + "_" + AREA_LIMIT),
                    new CacheCallBack<List<Area>>() {
                        @Override
                        public List<Area> loadData() {
                            return Area.findTopAreas(goodsCond.districtId,
                                    AREA_LIMIT, goodsCond.areaId);
                        }
                    });*/


            final GoodsCondition brandCond = new GoodsCondition(condition);
            brandCond.brandId = 0;

            BreadcrumbList breadcrumbs = createBreadcrumbs(goodsCond, keywords);

            renderGoodsCond(goodsCond);
            renderGoodsListTitle(goodsCond);
            render("/Goods2/index.html", onSaleGoodsCount, goodsPage, recommendGoodsList, searchAreas,
                    districts, searchCategories, subCategories, breadcrumbs);
        } catch (Exception e) {
            e.printStackTrace();
            index();
        }
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
                        "BREADCRUMBS"), CacheHelper.getCacheKey(
                new String[]{Order.CACHEKEY_BASEUSERID + userId,
                        models.sales.Goods.CACHEKEY_BASEID + id},
                "LIMITNUMBER"));
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
        final Date currentDate = new Date();
        //右上侧图片展示
        List<Block> rightSlides = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "RIGHT_SLIDES"), new CacheCallBack<List<Block>>() {
            @Override
            public List<Block> loadData() {
                return Block.findByType(BlockType.WEBSITE_RIGHT_SLIDE, currentDate);
            }
        });

        //热卖商品，销量最多的商品
        List<models.sales.Goods> hotSaleGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_HOT_SALE4"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return models.sales.Goods.findTopHotSale(4);
            }
        });
        //感兴趣的商品
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[]{models.sales.Goods.CACHEKEY,
                        models.sales.Goods.CACHEKEY_BASEID + goods.id},
                        "SHOW_TOP5RECOMMEND"),
                new CacheCallBack<List<models.sales.Goods>>() {
                    @Override
                    public List<models.sales.Goods> loadData() {
                        return models.sales.Goods.findSupplierTopRecommend(5, goods);
                    }
                });
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

        String tjUrl = "http://www." + play.Play.configuration.getProperty("application.baseDomain") + "/g/" + goods.id;
        if (user != null) {
            user.generatePromoterCode();
            tjUrl += "?tj=" + user.promoterCode;
        } else {
            tjUrl += "?tj=gshare";
        }

        renderArgs.put("tjUrl", tjUrl);
        renderArgs.put("hotSaleGoodsList", hotSaleGoodsList);
        renderArgs.put("browsedGoodsList", browsedGoodsList);
        renderArgs.put("rightSlides", rightSlides);
        renderArgs.put("goods", goods);
        renderArgs.put("imagesList", goods.getCachedGoodsImagesList());
        renderArgs.put("breadcrumbs", breadcrumbs);
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
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[]{models.sales.Goods.CACHEKEY,
                        models.sales.Goods.CACHEKEY_BASEID + goods.id},
                        "SHOW_TOP5RECOMMEND"),
                new CacheCallBack<List<models.sales.Goods>>() {
                    @Override
                    public List<models.sales.Goods> loadData() {
                        return models.sales.Goods.findSupplierTopRecommend(5, goods);
                    }
                });

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
        renderArgs.put("recommendGoodsList", recommendGoodsList);
    }


    private static BreadcrumbList getGoodsBreadCrumbs(long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        long categoryId = 0;
        if (goods.categories != null && goods.categories.size() > 0) {
            Category category = goods.categories.iterator().next();
            categoryId = category.id;
            breadcrumbs.append(category.name, "/s/" + category.id);
        }
        if (goods.brand != null) {
            breadcrumbs.append(goods.brand.name, "/s/" + categoryId + "-021-"
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
        System.out.println(questions.size());
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

    private static void renderGoodsCond(GoodsCondition goodsCond) {
        renderArgs.put("categoryId", (goodsCond.parentCategoryId != 0) ? goodsCond.parentCategoryId : goodsCond.categoryId);
        renderArgs.put("cityId", goodsCond.cityId);
        renderArgs.put("districtId", goodsCond.districtId);
        renderArgs.put("areaId", goodsCond.areaId);
        renderArgs.put("searchAreaId", goodsCond.searchAreaId);
        renderArgs.put("brandId", goodsCond.brandId);
        renderArgs.put("priceFrom", goodsCond.priceFrom);
        renderArgs.put("priceTo", goodsCond.priceTo);
        renderArgs.put("orderBy", goodsCond.orderByNum);
        renderArgs.put("orderByType", goodsCond.orderByTypeNum);
        renderArgs.put("materialType", goodsCond.materialType);
    }

    private static void renderGoodsListTitle(GoodsCondition goodsCond) {
        List<String> titleList = new ArrayList<>();
        if (goodsCond.categoryId > 0) {
            Category c = Category.findById(goodsCond.categoryId);
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
        if (goodsCond.brandId > 0) {
            Brand brand = Brand.findById(goodsCond.brandId);
            titleList.add(brand.name);
        }

        if (titleList.size() > 0) {
            renderArgs.put("condsTitle", StringUtils.join(titleList, " "));
        } else {
            renderArgs.put("condsTitle", "所有商品");
        }
    }

    private static final String LIST_URL_HEAD = "/s/";

    private static BreadcrumbList createBreadcrumbs(GoodsCondition goodsCond, String keywords) {
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        if (goodsCond == null || goodsCond.isDefault() && StringUtils.isBlank(keywords)) {
            return breadcrumbs;
        }
        if (goodsCond.priceFrom.compareTo(BigDecimal.ZERO) > 0
                || goodsCond.priceTo.compareTo(BigDecimal.ZERO) > 0) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
            breadcrumbs.add(createDistrictCrumb(goodsCond));
            breadcrumbs.add(createAreaCrumb(goodsCond));
            breadcrumbs.add(createBrandCrumb(goodsCond));
            breadcrumbs.add(new Breadcrumb(goodsCond.getPriceScopeExpress(),
                    goodsCond.getUrl()));
        } else if (goodsCond.brandId > 0) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
            breadcrumbs.add(createDistrictCrumb(goodsCond));
            breadcrumbs.add(createAreaCrumb(goodsCond));
            breadcrumbs.add(createBrandCrumb(goodsCond));
        } else if (GoodsCondition.isValidAreaId(goodsCond.areaId)) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
            breadcrumbs.add(createDistrictCrumb(goodsCond));
            breadcrumbs.add(createAreaCrumb(goodsCond));
        } else if (GoodsCondition.isValidAreaId(goodsCond.districtId)) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
            breadcrumbs.add(createDistrictCrumb(goodsCond));
        } else if (goodsCond.categoryId > 0) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
        }
        return breadcrumbs;
    }

    private static Breadcrumb createBrandCrumb(GoodsCondition goodsCond) {
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI
                + '-' + goodsCond.districtId + '-' + goodsCond.areaId + '-'
                + goodsCond.brandId;
        String desc;
        if (goodsCond.brandId > 0) {
            Brand brand = Brand.findById(goodsCond.brandId);
            desc = brand.name;
        } else {
            desc = "全部品牌";
        }
        return new Breadcrumb(desc, url);
    }

    private static Breadcrumb createCategoryCrumb(GoodsCondition goodsCond) {
        String url = LIST_URL_HEAD + goodsCond.categoryId;
        String desc;
        if (goodsCond.categoryId > 0) {
            Category category = Category.findById(goodsCond.categoryId);
            desc = category.name;
        } else {
            desc = "全部分类";
        }
        return new Breadcrumb(desc, url);
    }

    private static Breadcrumb createAreaCrumb(GoodsCondition goodsCond) {
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI
                + '-' + goodsCond.districtId + '-' + goodsCond.areaId;
        String desc;
        if (GoodsCondition.isValidAreaId(goodsCond.areaId)) {
            Area area = Area.findById(goodsCond.areaId);
            desc = area.name;
        } else {
            desc = "全部商圈";
        }
        return new Breadcrumb(desc, url);
    }

    private static Breadcrumb createDistrictCrumb(GoodsCondition goodsCond) {
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI
                + '-' + goodsCond.districtId;
        String desc;
        if (GoodsCondition.isValidAreaId(goodsCond.districtId)) {
            Area district = Area.findById(goodsCond.districtId);
            desc = district.name;
        } else {
            desc = "全部地区";
        }
        return new Breadcrumb(desc, url);
    }

}
