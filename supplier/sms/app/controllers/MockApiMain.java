package controllers;

import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * User: tanglq
 * Date: 13-1-16
 * Time: 上午11:46
 */
public class MockApiMain extends Controller {

    @Before
    public static void checkMockSwitch() {
        String canMock = Play.configuration.getProperty("mock.api.ui", "disable");

        if (!"enabled".equals(canMock)) {
            error(404, "Can't use.");
        }
    }

    public static void index() {
        render();
    }
}
