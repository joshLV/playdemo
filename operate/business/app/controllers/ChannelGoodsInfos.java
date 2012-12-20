package controllers;

import models.resale.Resaler;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;
import models.sales.Goods;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-20
 * Time: 上午11:06
 */
@With(OperateRbac.class)
@ActiveNavigation("channle_index")
public class ChannelGoodsInfos extends Controller {
    @ActiveNavigation("channle_index")
    public static void index(Long goodsId) {
        Goods goods = Goods.findUnDeletedById(goodsId);
        List<Resaler> resalerList = Resaler.findByStatus();
        render(resalerList, goods);
    }


    public static void create() {
        render();
    }
}
