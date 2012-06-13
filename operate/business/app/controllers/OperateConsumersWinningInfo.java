package controllers;

import models.cms.VoteType;
import models.consumer.UserVote;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 下午4:14
 */
@With(OperateRbac.class)
@ActiveNavigation("votes_index")
public class OperateConsumersWinningInfo extends Controller {
    private static final int PAGE_SIZE = 15;


    public static void index(VoteType type) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator votePage = UserVote.getPage(pageNumber, PAGE_SIZE,type);
        System.out.println(">>>>>>>>>>>"+votePage.size());
        render("/WinningInfo/index.html",votePage);
    }
}
