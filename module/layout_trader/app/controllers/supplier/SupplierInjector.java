package controllers.supplier;

import com.uhuila.common.constants.PlatformType;
import models.cms.Topic;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/28/13
 * Time: 4:59 PM
 */
public class SupplierInjector extends Controller {
    public static final String SESSION_MSG_SHOW_KEY = "session_msg_show_key";

    @Before
    public static void index() {
        //商户系统公告
        Topic topic = Topic.getTopValid(PlatformType.SUPPLIER);

        if (session.get(SESSION_MSG_SHOW_KEY) == null || !session.get(SESSION_MSG_SHOW_KEY).equals("close")) {
            renderArgs.put("topic", topic);
        }
    }

    public static void closeMessage() {
        session.put(SESSION_MSG_SHOW_KEY, "close");
    }
}