package controllers;

import models.job.qingtuan.QTSpider;
import play.mvc.Controller;

/**
 * @author likang
 *         Date: 12-11-9
 */
public class QingtuanLauncher extends Controller {
    public static void index() throws Exception {
        QTSpider qtSpider = new QTSpider();
        qtSpider.doJob();
        renderText("已经启动了，等等吧");
    }
}
