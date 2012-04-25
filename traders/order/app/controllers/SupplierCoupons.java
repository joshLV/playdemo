package controllers;

import java.util.List;
import java.util.Map;

import models.admin.SupplierSetting;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With(SupplierRbac.class)
public class SupplierCoupons extends Controller {

	public static int PAGE_SIZE = 15;

	/**
	 * 验证页面
	 */
	@ActiveNavigation("coupon_index")
	public static void index() {
		Long supplierId = SupplierRbac.currentUser().supplier.id;
		Long supplierUserId = SupplierRbac.currentUser().id;
		SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
		List shopList = Shop.findShopBySupplier(supplierId);
		SupplierSetting supplierSetting = SupplierSetting.getSetting(supplierUserId);
		render("SupplierCoupons/index.html",shopList,supplierUser,supplierSetting);
	}

	/**
	 * 查询
	 *
	 * @param eCouponSn 券号
	 */
	public static void queryCoupons(Long shopId,String eCouponSn) {

		if (Validation.hasErrors()) {
			params.flash();
			Validation.keep();
			renderTemplate("SupplierCoupons/index.html", eCouponSn);
		}

		Long supplierId = SupplierRbac.currentUser().supplier.id;
	
		//根据页面录入券号查询对应信息
		Map<String, Object> queryMap = ECoupon.queryInfo(eCouponSn, supplierId,shopId);
		renderJSON(queryMap);
	}

	/**
	 * 修改券状态,并产生消费交易记录
	 *
	 * @param eCouponSn 券号
	 */
	public static void update(Long shopId,String eCouponSn,String shopName) {
		if (Validation.hasErrors()) {
			params.flash();
			Validation.keep();
			renderTemplate("SupplierCoupons/index.html", eCouponSn);
		}

		Long supplierId = SupplierRbac.currentUser().supplier.id;
		ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
		//根据页面录入券号查询对应信息,并产生消费交易记录
		if (eCoupon == null){
			renderJSON("err");
		}
		eCoupon.consumed(shopId);

		SupplierSetting supplierSetting = new SupplierSetting();
		supplierSetting.save(SupplierRbac.currentUser().id,shopId,shopName);
		renderJSON("0");
	}

	/**
	 * 券号列表
	 */
	@ActiveNavigation("coupons")
	public static void coupons() {
		Long supplierId = SupplierRbac.currentUser().supplier.id;
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		ModelPaginator<models.order.ECoupon> couponsList = ECoupon.queryCoupons(supplierId, pageNumber, PAGE_SIZE);
		render("SupplierCoupons/e_coupons.html", couponsList);
	}
}
