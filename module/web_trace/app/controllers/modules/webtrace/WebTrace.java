package controllers.modules.webtrace;

import play.*;
import play.mvc.*;

public class WebTrace extends Controller {

    @Before
    public static void addTrace() {
        Http.Cookie cookieId = request.cookies.get("identity");
        if (cookieId != null){
            response.setCookie("identity",session.getId());
        }
    }

}
