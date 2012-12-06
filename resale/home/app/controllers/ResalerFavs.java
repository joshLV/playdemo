package controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import models.resale.Resaler;
import models.resale.ResalerFav;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 分销商分销库控制器，提供http接口对分销库进行增删该查
 *
 * @author likang
 *
 */
@With(SecureCAS.class)
public class ResalerFavs extends Controller {

	/**
	 * 分销库主界面
	 */
	public static void index(Date createdAtBegin, Date createdAtEnd, String goodsName, Long goodsId) {
		Resaler resaler = SecureCAS.getResaler();
		List<ResalerFav> favs = ResalerFav.findFavs(resaler,createdAtBegin, createdAtEnd,
				goodsName, goodsId);
		renderArgs.put("createdAtBegin", createdAtBegin);
		renderArgs.put("createdAtEnd", createdAtEnd);
		renderArgs.put("goodsName", goodsName);
		render(favs, resaler);
	}

	/**
	 * 加入或修改购物车列表
	 *
	 * @param goodsIds  商品ID
	 */
	public static void order(@As(",") Long... goodsIds) {
		Resaler resaler = SecureCAS.getResaler();
		Map<String,String> map = ResalerFav.checkGoods(resaler,goodsIds);
		renderJSON(map);

	}


	/**
	 * 从购物车中删除指定商品列表
	 *
	 * @param goodsIds 商品列表
	 */
	public static void delete(@As(",") List<Long> goodsIds) {
		Resaler resaler = SecureCAS.getResaler();

		ResalerFav.delete(resaler, goodsIds);

		ok();
	}
}
