package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import play.mvc.Controller;
import play.mvc.With;
import models.sales.Goods;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-13
 * Time: 上午10:38
 */
@With(SecureCAS.class)
public class ObtainGoldenCoins extends Controller {

    /**
     * 取得消费者签到次数和金币数
     */
    public static void index(Long gid) {
        User user = SecureCAS.getUser();
        Goods goods = null;
        if (gid != null) {
            goods = Goods.findOnSale(gid);
        }
        UserGoldenCoin.checkin(user, goods, "每天签到");
        String coinInfo = UserGoldenCoin.getCoinsInfo(user);
        renderJSON(coinInfo);
    }


}
