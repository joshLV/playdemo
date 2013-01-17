package controllers;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.CmsQuestion;
import models.cms.GoodsType;
import models.consumer.UserInfo;
import models.sales.PointGoodsCondition;
import models.sales.*;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 *        积分商品控制器
 * User: hejun
 * Date: 12-8-3
 * Time: 上午10:01
 */

@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class PointGoods extends Controller {

    public static String SHANGHAI = "021";
    public static int LIMIT = 13;
    public static int AREA_LIMIT = 11;
    public static int PAGE_SIZE = 18;

    public static void index(){

        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        // 网友推荐商品
        List<models.sales.Goods> recommendGoodsList = models.sales.Goods
                .findTopRecommend(4);

        // 设置列表展示商品 过滤条件
        PointGoodsCondition goodsCond = new PointGoodsCondition();
        goodsCond.status = GoodsStatus.ONSALE;
        goodsCond.baseSaleBegin = 1;
        goodsCond.expireAtBegin = new Date();

        // 获得列表商品
        List<models.sales.PointGoods> goodsList = models.sales.PointGoods.findByCondition(goodsCond,1,60);

        render(recommendGoodsList, goodsList);

    }


    /**
     * 积分商品详情.
     *
     * @param id 商品
     */
    public static void show(long id) {
        Http.Cookie idCookie = request.cookies.get("identity");
        String cookieValue = idCookie == null ? null : idCookie.value;
        Long userId = SecureCAS.getUser() == null ? null : SecureCAS
                .getUser().getId();


        // 根据id 找对应商品
        models.sales.PointGoods pointGoods = models.sales.PointGoods.findById(id);

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

        // 传递用户现有积分数
        if (userId != null){
            UserInfo userInfo = UserInfo.findById(userId);
            renderArgs.put("totalPoints",userInfo.totalPoints);
        }

        /*
        List<CmsQuestion> questions = CacheHelper.getCache(CacheHelper
                .getCacheKey(models.sales.PointGoods.CACHEKEY_BASEID + id,
                        "QUESTION_u" + userId + "_c" + cookieValue),
                new CacheCallBack<List<CmsQuestion>>() {
                    @Override
                    public List<CmsQuestion> loadData() {
                        return CmsQuestion.findOnGoodsShow(userId, cookieValue,
                                id, GoodsType.POINTGOODS, 0, 10);
                    }
                }); */

        List<CmsQuestion> questions = CmsQuestion.findOnGoodsShow(userId,cookieValue,id,GoodsType.POINTGOODS,0,10);
        // 传递积分商品咨询问题
        renderArgs.put("questions", questions);

        render();
    }

    private static void showGoods(final models.sales.PointGoods goods) {
        if (goods == null) {
            notFound();
        }
        // 热门积分兑换商品
        List<models.sales.PointGoods> recommendPointGoodsList = models.sales.PointGoods.findTopSaleGoods(3);
        //System.out.println("Recommend list length   ========== "+recommendPointGoodsList.size());

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
        Long boughtNumber = 0l;
        int addCartNumber = 0;
        renderArgs.put("boughtNumber",boughtNumber);
        renderArgs.put("goods", goods);
        renderArgs.put("recommendPointGoodsList",recommendPointGoodsList);
        renderArgs.put("recommendGoodsList", recommendGoodsList);
    }

    // 人气指数统计
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

}
