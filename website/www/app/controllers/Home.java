package controllers;

import java.util.Date;
import java.util.List;
import models.cms.Block;
import models.cms.BlockType;
import models.sales.Area;
import models.sales.Category;
import play.mvc.Controller;
import play.mvc.With;
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

    public static void index(long categoryId) {
        //精选商品
        List<models.sales.Goods> goodsList;
        if (categoryId == 0) {
            goodsList = models.sales.Goods.findTop(5);
        } else {
            goodsList = models.sales.Goods.findTopByCategory(categoryId, 5);
        }
        //近日成交商品
        List<models.sales.Goods> recentGoodsList = models.sales.Goods.findTradeRecently(5);

        //网友推荐商品
        List<models.sales.Goods> recommendGoodsList = models.sales.Goods.findTopRecommend(4);

        //前n个区域
        List<Area> districts = Area.findTopDistricts(Goods.SHANGHAI, 12);
        //前n个商圈
        List<Area> areas = Area.findTopAreas(13);
        List<Category> categories = Category.findTop(8);
        renderArgs.put("categoryId", categoryId);
        
        // CMS 区块
        Date currentDate = new Date();
        List<Block> slides = Block.findByType(BlockType.WEBSITE_SLIDE, currentDate);
        List<Block> dailySpecials = Block.findByType(BlockType.DAILY_SPECIAL, currentDate);
        renderArgs.put("slides", slides);
        renderArgs.put("dailySpecials", dailySpecials);

        render(goodsList, recentGoodsList, recommendGoodsList, categories, districts, areas);
    }
}
