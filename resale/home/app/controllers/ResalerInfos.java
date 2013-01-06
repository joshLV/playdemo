package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.resale.Resaler;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import static play.Logger.warn;

@With(SecureCAS.class)
public class ResalerInfos extends Controller {

    /**
     * 修改资料页面
     */
    public static void index() {
        Resaler resaler = SecureCAS.getResaler();
        render(resaler);
    }

    /**
     * 修改资料
     */
    public static void update(@Valid Resaler resaler) {
        if (Validation.hasErrors()) {
            Validation.keep();
            for (String key : validation.errorsMap().keySet()) {
                warn("update: validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            index();
        }
        Resaler.updateInfo(SecureCAS.getResaler().getId(), resaler);
        renderArgs.put("flag", "1");
        resaler = SecureCAS.getResaler();
        render("ResalerInfos/index.html", resaler);
    }
}
