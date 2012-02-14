package controllers.modules.webtrace;

import play.*;
import play.mvc.*;

public class WebTrace extends Controller {

    @Before
    public static void addTrace() {
        Logger.debug("[WebTrace]: check cookie identity");
        Http.Cookie cookieId = request.cookies.get("identity");
        if (cookieId == null){
            Logger.debug("[WebTrace]: set a new cookie identity");
            response.setCookie("identity",session.getId(),"365d");
        }
    }

}
