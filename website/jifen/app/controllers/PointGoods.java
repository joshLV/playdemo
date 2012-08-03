package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.CmsQuestion;
import models.consumer.User;
import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import models.sales.*;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.List;

/**
 *        积分商品控制器
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-3
 * Time: 上午10:01
 * To change this template use File | Settings | File Templates.
 */

@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class PointGoods extends Controller {

    public static String SHANGHAI = "021";
    public static int LIMIT = 13;
    public static int AREA_LIMIT = 11;
    public static int PAGE_SIZE = 18;

    public static void index(){
        render();
    }


    /**
     * 积分商品详情.
     *
     * @param id 商品
     */
    public static void show(final long id) {
        Http.Cookie idCookie = request.cookies.get("identity");
        final String cookieValue = idCookie == null ? null : idCookie.value;
        final Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();

        models.sales.PointGoods pointGoods = models.sales.PointGoods.findById(id);

        System.out.println("找到积分商品-------"+pointGoods.name);

        if (pointGoods == null) {
            error(404, "没有找到该商品！");
        }

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

        showGoods(pointGoods);

        // CMSQuestion 需要重写，这个是用goods的，为了测试用
        List<CmsQuestion> questions = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.PointGoods.CACHEKEY_BASEID + id,
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

    private static void showGoods(final models.sales.PointGoods goods) {
        if (goods == null) {
            notFound();
        }
        // 热门积分兑换商品
        List<models.sales.PointGoods> recommendPointGoodsList = models.sales.PointGoods.findTopRecommend(3);
        System.out.println("rec list length   ========== "+recommendPointGoodsList.size());

        // 网友推荐商品
        List<models.sales.Goods> recommendGoodsList = CacheHelper.getCache(
                CacheHelper.getCacheKey(new String[] {models.sales.Goods.CACHEKEY,
                        models.sales.Goods.CACHEKEY_BASEID + goods.id},
                        "SHOW_TOP5RECOMMEND"),
                new CacheCallBack<List<models.sales.Goods>>() {
                    @Override
                    public List<models.sales.Goods> loadData() {
                        return models.sales.Goods.findTopRecommend(5);
                    }
                });

        GoodsStatistics.addVisitorCount(goods.id);
        renderArgs.put("goods", goods);
        renderArgs.put("recommendPointGoodsList",recommendPointGoodsList);
        renderArgs.put("recommendGoodsList", recommendGoodsList);
    }

}
