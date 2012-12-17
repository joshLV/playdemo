package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import models.sales.Brand;
import models.sales.GoodsCondition;
import models.sales.GoodsStatus;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 分销商商品列表控制器
 *
 * @author yanjy
 */
@With(SecureCAS.class)
public class ResalerGoods extends Controller {
    public static int PAGE_SIZE = 12;
    public static int LIMIT = 8;
    public static int MAX_LIMIT = Integer.MAX_VALUE;

    /**
     * 商品列表主界面
     */
    public static void index() {
        Resaler resaler = SecureCAS.getResaler();
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        GoodsCondition goodsCond = new GoodsCondition();
        goodsCond.status = GoodsStatus.ONSALE;
        JPAExtPaginator<models.sales.Goods> goodsList = models.sales
                .Goods.findByResaleCondition(resaler, goodsCond, pageNumber, PAGE_SIZE);
        List<Brand> brands = Brand.findTop(MAX_LIMIT);
        renderGoodsCond(goodsCond);
        render(goodsList, brands, resaler);
    }

    /**
     * 商品列表根据条件查询
     *
     * @param condition 查询条件
     */
    public static void list(String condition) {
        Resaler resaler = SecureCAS.getResaler();
        boolean isResaler = true;
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        GoodsCondition goodsCond = new GoodsCondition(isResaler, condition);
        goodsCond.status = GoodsStatus.ONSALE;
        JPAExtPaginator<models.sales.Goods> goodsList = models.sales
                .Goods.findByResaleCondition(resaler, goodsCond, pageNumber, PAGE_SIZE);
        List<Brand> brands = Brand.findTop(LIMIT, goodsCond.brandId);
        renderGoodsCond(goodsCond);
        render("ResalerGoods/index.html", goodsList, brands, resaler);
    }

    /**
     * 商品详情.
     *
     * @param id 商品
     */
    public static void show(long id) {
        Resaler resaler = SecureCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findUnDeletedById(id);
        if (goods == null) {
            notFound();
        }


        render(goods, resaler);
    }

    /**
     * 向页面设置选择信息
     *
     * @param goodsCond 页面设置选择信息
     */
    private static void renderGoodsCond(GoodsCondition goodsCond) {
        renderArgs.put("brandId", goodsCond.brandId);
        renderArgs.put("priceFrom", goodsCond.priceFrom);
        renderArgs.put("priceTo", goodsCond.priceTo);
        renderArgs.put("orderBy", goodsCond.orderByNum);
        renderArgs.put("orderByType", goodsCond.orderByTypeNum);
        renderArgs.put("materialType", goodsCond.materialType);
        renderArgs.put("type", goodsCond.type);
    }
}
