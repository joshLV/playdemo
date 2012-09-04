package controllers;

import java.util.List;

import play.mvc.Controller;
import cache.CacheCallBack;
import cache.CacheHelper;

public class TuanFeeds extends Controller {

    public static void tuan800(final long categoryId) {
        //精选商品        
        List<models.sales.Goods> goodsList = CacheHelper.getCache(CacheHelper.getCacheKey(models.sales.Goods.CACHEKEY, "WWW_TOPS"), new CacheCallBack<List<models.sales.Goods>>() {
            @Override
            public List<models.sales.Goods> loadData() {
                return getTopGoods(categoryId);
            }
        });
        
        render(goodsList);
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
