package controllers;

import java.util.Date;

import models.resale.Resaler;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class ResalerInfos extends Controller {

	/**
	 * 修改资料页面
	 */
	public static void index(){
		Resaler resaler = SecureCAS.getResaler();
		render(resaler);
	}

	/**
	 * 修改资料
	 */
	public static void update(Long id, @Valid Resaler resaler){
		if (Validation.hasErrors()) {
			resaler.id = id;
			render("ResalerInfos/index.html", resaler);
		}
		resaler.updateInfo(id,resaler);
		index();
	}
}
