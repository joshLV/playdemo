package controllers;

import com.uhuila.common.util.DateUtil;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import models.sales.CheckinRelations;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 上午11:00
 */
@With(OperateRbac.class)
@ActiveNavigation("golden_coins_reports")
public class GoldenCoinReports extends Controller {
    private static final int PAGE_SIZE = 30;

    /**
     * 金币明细
     */
    public static void index(UserCondition condition) {

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new UserCondition();
            condition.createdAtBegin = DateUtil.getBeginOfDay();
            condition.createdAtEnd = DateUtil.getEndOfDay(new Date());
        }
        JPAExtPaginator<UserGoldenCoin> reportPage = UserGoldenCoin.find(null, condition, pageNumber, PAGE_SIZE);
        CheckinRelations summary = CheckinRelations.summary(reportPage);

        render(reportPage, condition, summary);
    }


    /**
     * 签到记录
     */
    @ActiveNavigation("checkin_reports")
    public static void checkin(UserCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new UserCondition();
            condition.createdAtBegin = DateUtil.getBeginOfDay();
            condition.createdAtEnd = DateUtil.getEndOfDay(new Date());
        }
        // 查询出所有结果
        List<CheckinRelations> resultList = CheckinRelations.getCheckinList(condition);
        // 分页
        ValuePaginator<CheckinRelations> reportPage = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        Long summary = CheckinRelations.checkinSummary(resultList);

        render(reportPage, condition, summary);
    }
}
