package controllers;

import play.modules.router.Get;
import play.mvc.Controller;

/**
 * User: tanglq
 * Date: 13-1-16
 * Time: 上午11:46
 */
public class MockApiMain extends Controller {

    @Get("/mock-api")
    public static void index() {
        render();
    }
}
