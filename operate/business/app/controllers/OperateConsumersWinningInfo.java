package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.consumer.UserVote;
import models.consumer.UserVoteCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
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

    @ActiveNavigation("votes_index")
    public static void index(UserVoteCondition condition) {
        String page = request.params.get("page");
        if (condition == null) {
            condition = new UserVoteCondition();
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<UserVote> votePage = UserVote.getPage(pageNumber, PAGE_SIZE, condition);
        render("/WinningInfo/index.html", votePage, condition);
    }

    public static void delete(Long id) {
        UserVote vote = UserVote.findById(id);
        vote.deleted = DeletedStatus.DELETED;
        vote.save();
        index(null);
    }
}
