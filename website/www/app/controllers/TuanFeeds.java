package controllers;

import java.util.ArrayList;
import java.util.List;

import models.sales.Category;
import org.postgresql.translation.messages_bg;
import play.i18n.Messages;
import play.mvc.Controller;
import cache.CacheCallBack;
import cache.CacheHelper;

public class TuanFeeds extends Controller {
    public static void tuan800(final long categoryId) {
        List<models.sales.Goods> goodsList = getTopGoods(categoryId, "tuan800category");
        render(goodsList);
    }

    public static void tuan360(final long categoryId) {
        List<models.sales.Goods> goodsList = getTopGoods(categoryId, "tuan360category");
        render(goodsList);
    }

    private static List<models.sales.Goods> getTopGoods(final long categoryId, final String tuan) {
        List<models.sales.Goods> allGoods = null;
        List<models.sales.Goods> goodsList = new ArrayList<>();
        if (categoryId == 0) {
            allGoods = models.sales.Goods.findTop(6 * 5);
            String[] mailCategoriesId = new String[6 * 5];
            int i = 0;
            for (models.sales.Goods g : allGoods) {
                if (g.categories != null && g.categories.size() > 0 && g.categories.iterator() != null && g.categories.iterator().hasNext()) {
                    {
                        Category category = g.categories.iterator().next();
                        if (Messages.get(tuan + "." + category.id).contains(tuan)) {
                            {
                                mailCategoriesId[i++] = category.name;
                            }
                        } else {
                            goodsList.add(g);
                        }
                        if (goodsList.size() == 6) {
                            break;
                        }
                    }
                }
            }
        } else {
            allGoods = models.sales.Goods.findTopByCategory(categoryId, 6 * 5);
            String[] mailCategoriesId = new String[6 * 5];
            int i = 0;
            for (models.sales.Goods g : allGoods) {
                if (g.categories != null && g.categories.size() > 0 && g.categories.iterator() != null && g.categories.iterator().hasNext()) {
                    {
                        Category category = g.categories.iterator().next();
                        if (Messages.get(tuan + "." + category.id).contains(tuan)) {
                            {
                                mailCategoriesId[i++] = category.name;
                            }
                        } else {
                            goodsList.add(g);
                        }
                        if (goodsList.size() == 6) {
                            break;
                        }
                    }
                }
            }
        }
        return goodsList;
    }

}