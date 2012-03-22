package controllers.resaletrace;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import models.resale.Resaler;
import models.resaletrace.Cas;
import play.Logger;

public class ResaleCAS extends Controller {

/*
 *  在website-www中用于给未登陆的用户添加cookie唯一标识，在resale平台用暂时用不到
    @Before
    public static void addTrace() {
        Logger.debug("[ResaleCAS]: check cookie identity");
        Http.Cookie cookieId = request.cookies.get("identity");
        if (cookieId == null) {
            Logger.debug("[ResaleCAS]: set a new cookie identity");
            response.setCookie("identity", session.getId(), "365d");
        }
    }
*/

    @Before
    public static void setResaler() {
    	Resaler resaler = getResaler();
        Cas cas = new Cas();
        if (resaler != null) {
        	System.out.print(">>>>>>>>>>>>>>>>>>>>>>."+resaler.loginName);
            cas.isLogin = true;
            cas.loginName = resaler.loginName;
            cas.resaler = resaler;
        }
        renderArgs.put("cas", cas);
    }

    public static Resaler getResaler() {
        String loginName = session.get("loginName");
        
        if (loginName == null || "".equals(loginName)) {
            return null;
        }
        return Resaler.find("byloginName", loginName).first();
    }
}
