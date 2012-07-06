package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.Block;
import models.cms.BlockType;
import models.cms.CmsQuestion;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.*;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.Breadcrumb;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.After;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商品控制器.
 * <p/>
 * User: sujie Date: 2/13/12 Time: 5:32 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Goods extends Controller {

    public static String SHANGHAI = "021";
    public static int LIMIT = 13;
    public static int AREA_LIMIT = 11;
    public static int PAGE_SIZE = 18;

    /**
     * 商品列表初始页。 这个页面用得少，就不缓存了。
     */
    public static void index() {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        // 网友推荐商品
        List<models.sales.Goods> recommendGoodsList = models.sales.Goods
                .findTopRecommend(6);

        // 默认取出5页产品
        List<models.sales.Goods> goodsList = models.sales.Goods
                .findTop(PAGE_SIZE * 5);

        // 默认取出前n个上海的区
        List<Area> districts = Area.findTopDistricts(SHANGHAI, LIMIT);
        List<Area> areas = Area.findTopAreas(AREA_LIMIT);
        List<Category> categories = Category.findTop(LIMIT);
        List<Brand> brands = Brand.findTop(LIMIT);

        GoodsCondition goodsCond = new GoodsCondition();
        goodsCond.status = GoodsStatus.ONSALE;
        goodsCond.baseSaleBegin = 1;
        goodsCond.expireAtBegin = new Date();
        ValuePaginator<models.sales.Goods> goodsPage = new ValuePaginator<>(
                goodsList);
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(false);
        BreadcrumbList breadcrumbs = createBreadcrumbs(goodsCond);
        final Date currentDate = new Date();
        // 合作商家信息
        List<Block> suppliers = Block.findByType(BlockType.WEBSITE_SUPPLIER,
                currentDate);
        renderGoodsCond(goodsCond);
        renderGoodsListTitle(goodsCond);

        render(recommendGoodsList, suppliers, goodsPage, areas, districts,
                categories, brands, breadcrumbs);
    }

    /**
     * 商品列表按条件查询页.
     *
     * @param condition 查询条件
     */
    public static void list(String condition) {
        String page = params.get("page");
        final int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer
                .parseInt(page);

        try {

            final GoodsCondition goodsCond = new GoodsCondition(condition);
            goodsCond.status = GoodsStatus.ONSALE;
            goodsCond.baseSaleBegin = 1;
            goodsCond.expireAtBegin = new Date();

            CacheHelper.preRead(CacheHelper.getCacheKey(
                    models.sales.Goods.CACHEKEY, "LIST_TOPRECOMMEND"),
                    CacheHelper.getCacheKey(Area.CACHEKEY, "LIST_DISTRICTS"
                            + goodsCond.districtId + "_" + LIMIT), CacheHelper
                    .getCacheKey(Area.CACHEKEY, "LIST_AREAS"
                            + goodsCond.districtId + "_"
                            + goodsCond.areaId + "_" + AREA_LIMIT),
                    CacheHelper.getCacheKey(Category.CACHEKEY,
                            "LIST_CATEGORIES" + goodsCond.categoryId + "_"
                                    + LIMIT), CacheHelper.getCacheKey(
                    Brand.CACHEKEY, "LIST_BRANDS" + condition));

            // 网友推荐商品
            List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                    CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY,
                            "LIST_TOPRECOMMEND"),
                    new CacheCallBack<List<models.sales.Goods>>() {
                        @Override
                        public List<models.sales.Goods> loadData() {
                            return models.sales.Goods.findTopRecommend(3);
                        }
                    });

            JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods
                    .findByCondition(goodsCond, pageNumber, PAGE_SIZE);

            // 默认取出前n个上海的区
            List<Area> districts = CacheHelper.getCache(
                    CacheHelper.getCacheKey(Area.CACHEKEY, "LIST_DISTRICTS"
                            + goodsCond.districtId + "_" + LIMIT),
                    new CacheCallBack<List<Area>>() {
                        @Override
                        public List<Area> loadData() {
                            return Area.findTopDistricts(SHANGHAI, LIMIT,
                                    goodsCond.districtId);
                        }
                    });
            List<Area> areas = CacheHelper.getCache(
                    CacheHelper.getCacheKey(Area.CACHEKEY, "LIST_AREAS"
                            + goodsCond.districtId + "_" + goodsCond.areaId
                            + "_" + AREA_LIMIT),
                    new CacheCallBack<List<Area>>() {
                        @Override
                        public List<Area> loadData() {
                            return Area.findTopAreas(goodsCond.districtId,
                                    AREA_LIMIT, goodsCond.areaId);
                        }
                    });
            List<Category> categories = CacheHelper.getCache(CacheHelper
                    .getCacheKey(Category.CACHEKEY, "LIST_CATEGORIES"
                            + goodsCond.categoryId + "_" + LIMIT),
                    new CacheCallBack<List<Category>>() {
                        @Override
                        public List<Category> loadData() {
                            return Category
                                    .findTop(LIMIT, goodsCond.categoryId);
                        }
                    });
            final GoodsCondition brandCond = new GoodsCondition(condition);
            brandCond.brandId = 0;
            List<Brand> brands = CacheHelper.getCache(
                    CacheHelper.getCacheKey(Brand.CACHEKEY, "LIST_BRANDS"
                            + condition), new CacheCallBack<List<Brand>>() {
                @Override
                public List<Brand> loadData() {
                    return models.sales.Goods
                            .findBrandByCondition(brandCond);
                }
            });

            BreadcrumbList breadcrumbs = createBreadcrumbs(goodsCond);

            final Date currentDate = new Date();
            //合作商家信息
            List<Block> suppliers = CacheHelper.getCache(CacheHelper.getCacheKey(Block.CACHEKEY, "WWW_SUPPLIER"), new CacheCallBack<List<Block>>() {
                @Override
                public List<Block> loadData() {
                    return Block.findByType(BlockType.WEBSITE_SUPPLIER, currentDate);
                }
            });

            renderGoodsCond(goodsCond);
            renderGoodsListTitle(goodsCond);
            render("/Goods/index.html", goodsPage, recommendGoodsList, areas,
                    districts, categories, brands, breadcrumbs, suppliers);
        } catch (Exception e) {
            e.printStackTrace();
            index();
        }
    }

    public static void preview(String uuid, boolean isSupplier) {
        models.sales.Goods goods = models.sales.Goods.getPreviewGoods(uuid);
        if (goods == null) {
            error(404, "没有找到该商品！");
        }
        showGoods(goods);
        render("Goods/show.html", isSupplier);
    }

    private static void showGoods(final models.sales.Goods goods) {
        if (goods == null) {
            notFound();
        }
        // 登陆的场合，判断该会员是否已经购买过此限购商品
        final User user = SecureCAS.getUser();
        //该用户曾经购买该商品的数量
        Long boughtNumber = 0l;
        if (user != null) {

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

        // 网友推荐商品
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY,
                        "LIST_TOPRECOMMEND"),
                new CacheCallBack<List<models.sales.Goods>>() {
                    @Override
                    public List<models.sales.Goods> loadData() {
                        return models.sales.Goods.findTopRecommend(5);
                    }
                });
        GoodsStatistics.addVisitorCount(goods.id);
        renderArgs.put("goods", goods);
        renderArgs.put("shops", goods.getShopList());
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
     * 商品详情.
     *
     * @param id 商品
     */
    public static void show(final long id) {
        Http.Cookie idCookie = request.cookies.get("identity");
        final String cookieValue = idCookie == null ? null : idCookie.value;
        final Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();

        CacheHelper.preRead(CacheHelper.getCacheKey(
                models.sales.Goods.CACHEKEY_BASEID + id, "UNDELETED"),
                CacheHelper.getCacheKey(
                        models.sales.Goods.CACHEKEY_BASEID + id, "KEYWORDMAP"),
                CacheHelper.getCacheKey(
                        models.sales.Goods.CACHEKEY_BASEID + id, "QUESTION_u"
                        + userId + "_c" + cookieValue), CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "BREADCRUMBS"), CacheHelper.getCacheKey(
                new String[]{Order.CACHEKEY_BASEUSERID + userId,
                        models.sales.Goods.CACHEKEY_BASEID + id},
                "LIMITNUMBER"));
        models.sales.Goods goods = CacheHelper.getCache(CacheHelper
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
        // 增加商品推荐指数
//        models.sales.Goods.addRecommend(goods, false);

        // 设置导航栏位置及类别相关的关键字.
        Map<String, String> keywordsMap = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "KEYWORDMAP"),
                new CacheCallBack<Map<String, String>>() {
                    @Override
                    public Map<String, String> loadData() {
                        return generateKeywordsMap(id);
                    }
                });

        renderArgs.put("goodsKeywords", keywordsMap.get("goodsKeywords"));
        renderArgs.put("categoryId", keywordsMap.get("categoryId"));

        // 记录用户浏览过的商品
        Http.Cookie cookie = request.cookies.get("saw_goods_ids");
        String sawGoodsIds = ",";
        if (cookie != null) {
            if (!cookie.value.contains("," + id + ",")) {
                sawGoodsIds = "," + id + cookie.value;
                if (sawGoodsIds.length() > 100) {
                    sawGoodsIds = sawGoodsIds.substring(0, 100);
                }
            } else {
                sawGoodsIds = cookie.value;
            }
        }

        response.setCookie("saw_goods_ids", sawGoodsIds);

        showGoods(goods);

        List<CmsQuestion> questions = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.Goods.CACHEKEY_BASEID + id,
                        "QUESTION_u" + userId + "_c" + cookieValue),
                new CacheCallBack<List<CmsQuestion>>() {
                    @Override
                    public List<CmsQuestion> loadData() {
                        return CmsQuestion.findOnGoodsShow(userId, cookieValue,
                                id, 0, 10);
                    }
                });
        renderArgs.put("questions", questions);

        render();
    }

    private static Map<String, String> generateKeywordsMap(long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        Map<String, String> keywordsMap = new HashMap<>();

        List<String> categoryKeywords = new ArrayList<>();
        if (goods.categories != null) {
            for (Category c : goods.categories) {
                if (c.parentCategory != null) {
                    keywordsMap.put("categoryId",
                            String.valueOf(c.parentCategory.id));
                } else {
                    keywordsMap.put("categoryId", String.valueOf(c.id));
                }
                if (StringUtils.isNotBlank(c.keywords)) {
                    String[] ks = c.keywords.split("[,;\\s]+");
                    Collections.addAll(categoryKeywords, ks);
                }
            }
        }
        if (StringUtils.isNotBlank(goods.keywords)) {
            String[] ks = goods.keywords.split("[,;\\s]+");
            Collections.addAll(categoryKeywords, ks);
        }
        if (categoryKeywords.size() > 0) {
            keywordsMap.put("goodsKeywords",
                    StringUtils.join(categoryKeywords, ","));
        }
        return keywordsMap;
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
        renderJSON(statistics.summaryCount.toString());
    }

    /**
     * 清理预读缓存.
     */
    @After
    public static void clearCache() {
        CacheHelper.cleanPreRead();
    }

    private static void renderGoodsCond(GoodsCondition goodsCond) {
        renderArgs.put("categoryId", goodsCond.categoryId);
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

    private static BreadcrumbList createBreadcrumbs(GoodsCondition goodsCond) {
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        if (goodsCond.isDefault()) {
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
