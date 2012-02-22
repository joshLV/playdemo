package controllers.modules.webtrace;

import models.consumer.User;
import models.web_trace.Cas;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;

public class WebTrace extends Controller {

    @Before
    public static void addTrace() {
        Logger.debug("[WebTrace]: check cookie identity");
        Http.Cookie cookieId = request.cookies.get("identity");
        if (cookieId == null) {
            Logger.debug("[WebTrace]: set a new cookie identity");
            response.setCookie("identity", session.getId(), "365d");
        }
    }

    @Before
    public static void setUser() {
        User user = getUser();
        Cas cas = new Cas();
        if (user != null) {
            cas.isLogin = true;
            cas.loginName = user.loginName;
            cas.user = user;
        }
        renderArgs.put("cas", cas);
    }

    public static User getUser() {
        String username = session.get("username");
        if (username == null || "".equals(username)) {
            return null;
        }
        return User.find("byLoginName", username).first();
    }
}
