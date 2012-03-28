    package controllers;

import java.util.List;

import models.resale.Resaler;
import models.resale.ResalerFav;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

/**
 * 分销商分销库控制器，提供http接口对分销库进行增删该查
 *
 * @author likang
 *
 */
@With({SecureCAS.class, ResaleCAS.class})
public class ResalerFavs extends Controller {

    /**
     * 分销库主界面
     */
    public static void index() {
        Resaler resaler = ResaleCAS.getResaler();

        List<ResalerFav> favs = ResalerFav.findAll(resaler);
        render(favs);
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId  商品ID
     */
    public static void order(Long... goodsIds) {
        Resaler resaler = ResaleCAS.getResaler();
        for(long goodsId : goodsIds) {
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);       
            ResalerFav.order(resaler, goods);
        }
        index();
    }


    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsIds 商品列表
     */
    public static void delete(@As(",") List<Long> goodsIds) {
        Resaler resaler = ResaleCAS.getResaler();

        ResalerFav.delete(resaler, goodsIds);

        ok();
    }
}
