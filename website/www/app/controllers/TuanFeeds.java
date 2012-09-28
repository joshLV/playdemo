package controllers;

import java.util.List;

import play.mvc.Controller;
import cache.CacheCallBack;
import cache.CacheHelper;

public class TuanFeeds extends Controller {

    public static void tuan800(final long categoryId) {
        render(getTopGoods(categoryId));
    }

    public static void tuan360(final long categoryId) {
        render(getTopGoods(categoryId));
    }



    private static List<models.sales.Goods> getTopGoods(final long categoryId) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_TUAN_TOPS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                List<models.sales.Goods> goodsList = null;
                if (categoryId == 0) {
                    goodsList = models.sales.Goods.findTop(6);
                } else {
                    goodsList = models.sales.Goods.findTopByCategory(categoryId, 6);
                }
                return goodsList;
            }
        });
    }
    
}
