package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import play.Play;
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
		if (Validation.hasErrors()) {
            Validation.keep();
            index();
		}
		Resaler.updateInfo(SecureCAS.getResaler().getId(),resaler);
        renderArgs.put("flag", "1");
        resaler = SecureCAS.getResaler();
        render("ResalerInfos/index.html", resaler);
	}
}
