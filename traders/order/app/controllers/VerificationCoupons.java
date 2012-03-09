package controllers;

import java.util.Map;

import models.order.ECoupon;
import play.mvc.Controller;

public class VerificationCoupons  extends Controller {

	/**
	 * 验证页面
	 */
	public static void index(){
		System.out.println("aaaaaaaaaaaaaaaaaa");
		render("Verification/index.html");
	}

	/**
	 * 查询
	 */
	public static void queryCoupons(){
		String eCouponSn = params.get("eCouponSn");
		System.out.println(">>>>>>>>>>>>>>>"+eCouponSn);
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			renderTemplate("Verification/index.html",eCouponSn);
		}
		Long companyId=1l;
		//根据页面录入券号查询对应信息
		Map<String,Object> queryMap = ECoupon.query(eCouponSn,companyId);
		renderJSON(queryMap);
	}
	
}
