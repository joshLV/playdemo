package controllers;

import java.util.Map;

import models.accounts.Account;
import models.order.ECoupon;
import play.mvc.Controller;

public class VerificationCoupons  extends Controller {

	/**
	 * 验证页面
	 */
	public static void index(){
		render("Verification/index.html");
	}

	/**
	 * 查询
	 */
	public static void queryCoupons(String eCouponSn){
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			renderTemplate("Verification/index.html",eCouponSn);
		}
		Long companyId=1l;
		//根据页面录入券号查询对应信息
		Map<String,Object> queryMap = ECoupon.queryInfo(eCouponSn,companyId);
		renderJSON(queryMap);
	}
	
	/**
	 * 修改券状态,并产生消费交易记录
	 */
	public static void update(String eCouponSn){
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			renderTemplate("Verification/index.html",eCouponSn);
		}
		Long companyId=1l;
		//根据页面录入券号查询对应信息,并产生消费交易记录
		ECoupon.update(eCouponSn,companyId);
		
		renderJSON("");
	}
}
