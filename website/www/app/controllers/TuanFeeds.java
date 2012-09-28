package controllers;

import java.util.ArrayList;
import java.util.List;

import com.uhuila.common.util.DateUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.sales.Category;
import org.postgresql.translation.messages_bg;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;
import cache.CacheCallBack;
import cache.CacheHelper;

public class TuanFeeds extends Controller {
    public static void tuan800(final long categoryId) {
        List<models.sales.Goods> goodsList = getTopGoods(categoryId, "tuan800category", "团800", 6);
        render(goodsList);
    }

    public static void tuan360(final long categoryId) {
        List<models.sales.Goods> goodsList = getTopGoods(categoryId, "tuan360category", "360团", 6);
        render(goodsList);
    }

    private static List<models.sales.Goods> getTopGoods(final long categoryId, final String tuanCategory, final String tuanNane, int limit) {
        List<models.sales.Goods> allGoods = null;

        List<models.sales.Goods> goodsList = new ArrayList<>();
        if (categoryId == 0) {
            allGoods = models.sales.Goods.findTop(limit * 5);
            goodsList = filterTopGoods(allGoods, categoryId, tuanCategory, tuanNane, limit);

        } else {
            allGoods = models.sales.Goods.findTopByCategory(categoryId, limit * 5);
            goodsList = filterTopGoods(allGoods, categoryId, tuanCategory, tuanNane, limit);
        }
        return goodsList;
    }

    private static List<models.sales.Goods> filterTopGoods(List<models.sales.Goods> allGoods, final long categoryId, final String tuanCategory, final String tuanNane, int limit) {

        List<models.sales.Goods> goodsList = new ArrayList<>();
        List<Category> mailCategoryList = new ArrayList<Category>();
        int i = 0;
        for (models.sales.Goods g : allGoods) {
            if (g.categories != null && g.categories.size() > 0 && g.categories.iterator() != null && g.categories.iterator().hasNext()) {
                {
                    Category category = g.categories.iterator().next();
                    if (Messages.get(tuanCategory + "." + category.id).contains(tuanCategory)) {
                        {
                            mailCategoryList.add(category);
                        }
                    } else {
                        goodsList.add(g);
                    }
                    if (goodsList.size() == limit) {
                        break;
                    }
                }
            }
        }

        if (mailCategoryList.size() > 0) {
            //发送提醒邮件
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient("dev@uhuila.com");
            mailMessage.setSubject(Play.mode.isProd() ? tuanNane + "收录分类" : tuanNane + "收录分类【测试】");
            mailMessage.putParam("tuanName", tuanNane);
            mailMessage.putParam("mailCategoryList", mailCategoryList);
            MailUtil.sendTuanCategoryMail(mailMessage);
        }
        return goodsList;
    }
}