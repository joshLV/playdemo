package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

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
	public static void update(@Valid Resaler resaler){
          Resaler newResaler = SecureCAS.getResaler();

		if (Validation.hasErrors()) {
			render("ResalerInfos/index.html", resaler);
		}
		resaler.updateInfo(newResaler.id,resaler);
		index();
	}
}
