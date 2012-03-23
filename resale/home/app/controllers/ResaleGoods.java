package controllers;

import java.util.List;

import models.resale.ResaleGoodsCondition;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.sales.Brand;
import models.sales.Category;
import models.sales.GoodsCondition;

import org.apache.commons.lang.StringUtils;

import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

/**
 * 分销商商品列表控制器
 *
 * @author yanjy
 *
 */
@With({SecureCAS.class,ResaleCAS.class})
public class ResaleGoods extends Controller {
	public static int PAGE_SIZE = 6;
	/**
	 * 商品列表主界面
	 */
	public static void index() {
		Resaler resaler = ResaleCAS.getResaler();
		//TODO
		resaler= new Resaler();
		resaler.level = ResalerLevel.VIP1;
		String page = params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		ResaleGoodsCondition goodsCond = new ResaleGoodsCondition();
		JPAExtPaginator<models.sales.Goods> goodsList = models.sales
				.Goods.findByResaleCondition(goodsCond,pageNumber, PAGE_SIZE);
		List<Brand> brands = Brand.findTop(8, 0);
		render(goodsList,brands,resaler);
	}

	/**
	 * 商品列表根据条件查询
	 * @param condition 查询条件
	 */
	public static void list(String condition) {
		Resaler resaler = ResaleCAS.getResaler();
		String page = params.get("page");
		//TODO
		resaler= new Resaler();
		resaler.level = ResalerLevel.VIP1;

		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		ResaleGoodsCondition goodsCond = new ResaleGoodsCondition(condition);
		JPAExtPaginator<models.sales.Goods> goodsList = models.sales
				.Goods.findByResaleCondition(goodsCond,pageNumber, PAGE_SIZE);
		List<Brand> brands = Brand.findTop(8, goodsCond.brandId);
		renderGoodsCond(goodsCond);
		render("ResaleGoods/index.html",goodsList,brands,resaler);
	}

	/**
	 * 商品详情.
	 *
	 * @param id 商品
	 */
	public static void show(long id) {
		Resaler resaler = ResaleCAS.getResaler();
		//TODO
		resaler= new Resaler();
		resaler.level = ResalerLevel.VIP1;
		models.sales.Goods goods = models.sales.Goods.findUnDeletedById(id);
		if (goods == null) {
			notFound();
		}

		render(goods,resaler);
	}

	/**
	 * 向页面设置选择信息
	 * 
	 * @param goodsCond 页面设置选择信息
	 */
	private static void renderGoodsCond(ResaleGoodsCondition goodsCond) {
		renderArgs.put("brandId", goodsCond.brandId);
		renderArgs.put("priceFrom", goodsCond.priceFrom);
		renderArgs.put("priceTo", goodsCond.priceTo);
		renderArgs.put("orderBy", goodsCond.orderByNum);
		renderArgs.put("orderByType", goodsCond.orderByTypeNum);
	}
}
