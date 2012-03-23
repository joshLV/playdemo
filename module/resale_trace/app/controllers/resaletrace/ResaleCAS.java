package controllers.resaletrace;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import models.resale.Resaler;
import models.resaletrace.Cas;
import play.Logger;

public class ResaleCAS extends Controller {

    @Before
    public static void setResaler() {
    	Resaler resaler = getResaler();
        Cas cas = new Cas();
        if (resaler != null) {
            cas.isLogin = true;
            cas.username = resaler.loginName;
            cas.resaler = resaler;
        }
        renderArgs.put("cas", cas);
    }

    public static Resaler getResaler() {
        String loginName = session.get("username");
        
        if (loginName == null || "".equals(loginName)) {
            return null;
        }
        return Resaler.find("byLoginName", loginName).first();
    }
}
