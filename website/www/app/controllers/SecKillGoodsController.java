package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.order.OrderItems;
import models.sales.SecKillGoods;
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
        }
        //获取其他秒杀商品
        List<SecKillGoodsItem> secKillGoodsItems = SecKillGoodsItem.findSecKillGoods();
        User user = SecureCAS.getUser();
        //判断帐号限购
        boolean exceedLimit = false;
        if (user != null) {
            //判断帐号限购
            exceedLimit = checkLimitNumber(user, goodsItem.secKillGoods.goods.id, goodsItem.secKillGoods.id, 1);
        }

        render(goodsItem, secKillGoodsItems, exceedLimit);
    }

    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param user    用户ID
     * @param goodsId 商品ID
     * @param number  购买数量
     * @return
     */
    public static boolean checkLimitNumber(User user, Long goodsId, Long secKillGoodsId,
                                           long number) {

        long boughtNumber = OrderItems.getBoughtNumberOfSecKillGoods(user, goodsId, secKillGoodsId);
        //取出商品的限购数量
        models.sales.SecKillGoods goods = SecKillGoods.findById(secKillGoodsId);
        int limitNumber = 0;
        if (goods.personLimitNumber != null) {
            limitNumber = goods.personLimitNumber;
        }

        //超过限购数量,则表示已经购买过该商品
        return (limitNumber > 0 && (number > limitNumber || limitNumber <= boughtNumber));
    }
}