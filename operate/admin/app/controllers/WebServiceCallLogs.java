package controllers;

import models.journal.WebServiceCallLog;

import operate.rbac.annotations.ActiveNavigation;

import org.apache.commons.lang.StringUtils;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With(OperateRbac.class)
@ActiveNavigation("ws_call_logs")
public class WebServiceCallLogs extends Controller {
    
    public static void index(WebServiceCallLog log) {
        
        if (log == null) {
            log = new WebServiceCallLog();
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<WebServiceCallLog> logPage = WebServiceCallLog.query(log,
                        pageNumber, 30);

        render(log, logPage);
    }
}
