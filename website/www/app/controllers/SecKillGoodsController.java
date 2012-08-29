package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.order.OrderItems;
import models.sales.SecKillGoodsItem;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 秒杀商品控制器.
 * <p/>
 * User: sujie
 * Date: 8/15/12
 * Time: 11:18 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class SecKillGoodsController extends Controller {

    /**
     * 秒杀商品显示.
     * <p/>
     * 秒杀商品列表，当前正在秒杀商品详情，点击立即购买，直接下单购买.
     */
    @SkipCAS
    public static void index() {
        //获取当前秒杀商品
        SecKillGoodsItem goodsItem = SecKillGoodsItem.getCurrentSecKillGoods();
        if (goodsItem == null) {
            redirect("/s");
            return;
        }
        //获取其他秒杀商品
        List<SecKillGoodsItem> secKillGoodsItems = SecKillGoodsItem.findSecKillGoods();
        User user = SecureCAS.getUser();
        //判断帐号限购
        boolean exceedLimit = false;
        if (user != null) {
            //判断帐号限购
            exceedLimit = OrderItems.checkLimitNumber(user, goodsItem.secKillGoods.goods.id, goodsItem.secKillGoods.id, 1);
        }

        render(goodsItem, secKillGoodsItems, exceedLimit);
    }
}