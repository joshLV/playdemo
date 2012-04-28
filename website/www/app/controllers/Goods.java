package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.GoodsCondition;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.Breadcrumb;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 5:32 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Goods extends Controller {

    public static String SHANGHAI = "021";
    public static int LIMIT = 8;
    public static int PAGE_SIZE = 20;

    /**
     * 商品列表初始页
     */
    public static void index() {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        //默认取出5页产品
        List<models.sales.Goods> goodsList = models.sales.Goods.findTop(PAGE_SIZE * 5);
        //默认取出前8个上海的区
        List<Area> districts = Area.findTopDistricts(SHANGHAI, LIMIT);
        List<Area> areas = Area.findTopAreas(LIMIT);
        List<Category> categories = Category.findTop(LIMIT);
        List<Brand> brands = Brand.findTop(LIMIT);

        GoodsCondition goodsCond = new GoodsCondition();
        ValuePaginator<models.sales.Goods> goodsPage = new ValuePaginator<>(goodsList);
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(false);
        BreadcrumbList breadcrumbs = createBreadcrumbs(goodsCond);

        renderGoodsCond(goodsCond);
        render(goodsPage, areas, districts, categories, brands, breadcrumbs);
    }

    public static void preview(String uuid, boolean isSupplier) {
        models.sales.Goods goods = models.sales.Goods.getPreviewGoods(uuid);
        showGoods(goods);
        render(isSupplier);
    }

    private static void showGoods(models.sales.Goods goods) {
        if (goods == null) {
            notFound();
        }
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        long categoryId = 0;
        if (goods.categories != null && goods.categories.size() > 0) {
            Category category = goods.categories.iterator().next();
            categoryId = category.id;
            breadcrumbs.append(category.name, "/goods/list/" + category.id);
        }
        if (goods.brand != null) {
            breadcrumbs.append(goods.brand.name, "/goods/list/" + categoryId + "-" + SHANGHAI + "-0-0-" + goods.brand
                    .id);
        }
        renderArgs.put("goods", goods);
        renderArgs.put("breadcrumbs", breadcrumbs);

    }

    /**
     * 商品详情.
     *
     * @param id 商品
     */
    public static void show(long id) {
        models.sales.Goods goods = models.sales.Goods.findOnSale(id);
        if (goods == null) {
            error(404, "没有找到该商品！");
        }

        models.sales.Goods.addRecommend(goods, false);

        showGoods(goods);

        render();
    }

    /**
     * 用户喜欢指定商品.
     * todo 页面上还没有加这个功能
     *
     * @param id 商品标识
     */
    public static void like(long id) {
        models.sales.Goods goods = models.sales.Goods.findOnSale(id);

        models.sales.Goods.addRecommend(goods, true);

        //todo 添加到个人喜欢的商品列表中
        showGoods(goods);

        render("Goods/show.html");
    }

    /**
     * 商品列表按条件查询页.
     *
     * @param condition 查询条件
     */
    public static void list(String condition) {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        try {
            GoodsCondition goodsCond = new GoodsCondition(condition);
            JPAExtPaginator<models.sales.Goods> goodsPage = models.sales
                    .Goods.findByCondition(goodsCond, pageNumber, PAGE_SIZE);

            //默认取出前8个上海的区
            List<Area> districts = Area.findTopDistricts(SHANGHAI, LIMIT, goodsCond.districtId);
            List<Area> areas = Area.findTopAreas(goodsCond.districtId, LIMIT, goodsCond.areaId);
            List<Category> categories = Category.findTop(LIMIT, goodsCond.categoryId);
            List<Brand> brands = Brand.findTop(LIMIT, goodsCond.brandId);


            BreadcrumbList breadcrumbs = createBreadcrumbs(goodsCond);

            renderGoodsCond(goodsCond);
            render("/Goods/index.html", goodsPage, areas, districts, categories, brands, breadcrumbs);
        } catch (Exception e) {
            e.printStackTrace();
            index();
        }
    }

    private static void renderGoodsCond(GoodsCondition goodsCond) {
        renderArgs.put("categoryId", goodsCond.categoryId);
        renderArgs.put("cityId", goodsCond.cityId);
        renderArgs.put("districtId", goodsCond.districtId);
        renderArgs.put("areaId", goodsCond.areaId);
        renderArgs.put("brandId", goodsCond.brandId);
        renderArgs.put("priceFrom", goodsCond.priceFrom);
        renderArgs.put("priceTo", goodsCond.priceTo);
        renderArgs.put("orderBy", goodsCond.orderByNum);
        renderArgs.put("orderByType", goodsCond.orderByTypeNum);
    }

    private static final String LIST_URL_HEAD = "/goods/list/";

    private static BreadcrumbList createBreadcrumbs(GoodsCondition goodsCond) {
        BreadcrumbList breadcrumbs = new BreadcrumbList();
        if (goodsCond.isDefault()) {
            return breadcrumbs;
        }
        if (goodsCond.priceFrom.compareTo(BigDecimal.ZERO) > 0 || goodsCond.priceTo.compareTo(BigDecimal.ZERO) >
                0) {
            breadcrumbs.add(createCategoryCrumb(goodsCond));
            breadcrumbs.add(createDistrictCrumb(goodsCond));
            breadcrumbs.add(createAreaCrumb(goodsCond));
            breadcrumbs.add(createBrandCrumb(goodsCond));
            breadcrumbs.add(new Breadcrumb(goodsCond.getPriceScopeExpress(), goodsCond.getUrl()));
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
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI + '-' + goodsCond.districtId + '-' + goodsCond
                .areaId + '-' + goodsCond.brandId;
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
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI + '-' + goodsCond.districtId + '-' + goodsCond
                .areaId;
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
        String url = LIST_URL_HEAD + goodsCond.categoryId + '-' + SHANGHAI + '-' + goodsCond.districtId;
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
