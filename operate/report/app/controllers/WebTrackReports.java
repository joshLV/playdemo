package controllers;

import java.util.List;
import models.webop.WebTrackReferCodeCondition;
import models.webop.WebTrackReferCodeReport;
import models.webop.WebTrackRefererCondition;
import models.webop.WebTrackRefererReport;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;


@With(OperateRbac.class)
public class WebTrackReports extends Controller {

    private static final int PAGE_SIZE = 30;
    
    /**
     * 外链报表.
     */
    @ActiveNavigation("web_referer_reort")    
    public static void referers(WebTrackRefererCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new WebTrackRefererCondition();
        }

        List<WebTrackRefererReport> resultList = WebTrackRefererReport.queryRefererReport(condition);
        
        ValuePaginator<WebTrackRefererReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        WebTrackRefererReport summary = WebTrackRefererReport.summary(resultList);
        
        render(reportPage, summary, condition);        
    }
    
    /**
     * 推荐码报表.
     */
    @ActiveNavigation("web_tj_reort")    
    public static void refCodes(WebTrackReferCodeCondition condition) {
        int pageNumber = getPageNumber();

        if (condition == null) {
            condition = new WebTrackReferCodeCondition();
        }

        List<WebTrackReferCodeReport> resultList = WebTrackReferCodeReport.queryReferCodeReport(condition);
        
        ValuePaginator<WebTrackReferCodeReport> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        WebTrackReferCodeReport summary = WebTrackReferCodeReport.summary(resultList);
        
        render(reportPage, summary, condition);        
    }
    
    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
