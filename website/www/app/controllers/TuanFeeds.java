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
        List<models.sales.Goods> goodsList = models.sales.Goods.getTopGoods(categoryId, "tuan800category", "团800", 6);
        render(goodsList);
    }

    public static void tuan360(final long categoryId) {
        List<models.sales.Goods> goodsList = models.sales.Goods.getTopGoods(categoryId, "tuan360category", "360团", 6);
        render(goodsList);
    }

    public static void tuanBaidu(final long categoryId) {
        models.sales.Goods.getTopGoods(categoryId, "tuanBaiduCategory1", "百度团【一级分类】", 6);
        List<models.sales.Goods> goodsList = models.sales.Goods.getTopGoods(categoryId, "tuanBaiduCategory2", "百度团【二级分类】", 6);
        render(goodsList);
    }


}