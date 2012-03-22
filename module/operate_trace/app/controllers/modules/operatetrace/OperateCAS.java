package controllers.modules.operatetrace;

import models.supplier.Supplier;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 3/21/12
 * Time: 6:11 PM
 */
public class OperateCAS extends Controller {
    @Before
    public static void setUser() {
        Supplier supplier = getSupplier();
        Cas cas = new Cas();
        if (supplier != null) {
            cas.isLogin = true;
            cas.loginName = supplier.loginName;
            cas.user = supplier;
        }
        renderArgs.put("cas", cas);
    }

    public static Supplier getSupplier() {
        String username = session.get("supplier");
        if (username == null || "".equals(username)) {
            return null;
        }
        return Supplier.find("byLoginName", username).first();
    }

}