package controllers;

import models.order.PromoteRebate;
import models.order.PromoteRebateCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import utils.PaginateUtil;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-25
 * Time: 下午1:59
 */

@With(OperateRbac.class)
public class RebateRankReports extends Controller {
    public static int PAGE_SIZE = 15;

    @ActiveNavigation("rank_reports")
    public static void index(PromoteRebateCondition condition) {
        if (condition == null) {
            condition = new PromoteRebateCondition();
        }
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        // 查询出所有结果
        List<PromoteRebate> resultList = PromoteRebate.findRank(condition);

        // 分页
        ValuePaginator<PromoteRebate> rankList = PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);

        PromoteRebate summary = PromoteRebate.allRank(resultList);

        System.out.println(summary.promoteTimes + ";;;;;;;;;;;;;;;;;");

        render(rankList, summary, condition);
    }
}
