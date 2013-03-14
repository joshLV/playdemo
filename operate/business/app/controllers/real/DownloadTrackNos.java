package controllers.real;

import controllers.OperateRbac;
import models.order.DownloadTrackNoCondition;
import models.order.DownloadTrackNoReport;
import models.order.OrderShippingInfo;
import models.order.OuterOrderPartner;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.util.Date;
import java.util.List;

/**
 * 下载带运单号的跟踪表文件，供上传到不同.
 * User: tangl
 * Date: 13-3-12
 * Time: 下午5:54
 */
@With(OperateRbac.class)
@ActiveNavigation("download_track_no_for_resaler")
public class DownloadTrackNos extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 选择一个渠道，然后出现下载列表.
     *
     * @param
     */
    public static void index(DownloadTrackNoCondition condition) {

        int pageNumber = getPage();
        if (condition == null) {
            condition = new DownloadTrackNoCondition();
        }

        List<DownloadTrackNoReport> resultList = DownloadTrackNoReport.query(condition);
//        分页
        ValuePaginator<DownloadTrackNoReport> shippingList = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        render(shippingList, pageNumber, condition);
//        render(condition);
    }

    /**
     * 通过下载链接下载对应的excel文件.
     *
     * @param partner
     * @param outGoodsId
     */
    public static void download(OuterOrderPartner partner, Long outGoodsId) {

    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

}
