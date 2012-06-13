package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-11
 * Time: 下午1:49
 */
@With(OperateRbac.class)
@ActiveNavigation("votes_index")
public class WebVotes extends Controller {
    private static final int PAGE_SIZE = 15;


    public static void index(VoteType type) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator votePage = VoteQuestion.getPage(pageNumber, PAGE_SIZE, type);

        render(votePage, type);
    }

    @ActiveNavigation("votes_add")
    public static void add() {
        render();
    }


    public static void create(@Valid VoteQuestion vote) {
        checkExpireAt(vote);

        if (Validation.hasErrors()) {
            render("WebVotes/add.html", vote);
        }
        vote.deleted = DeletedStatus.UN_DELETED;
        vote.create();
        index(null);

    }


    public static void update(Long id, @Valid VoteQuestion vote) {
        checkExpireAt(vote);
        if (Validation.hasErrors()) {
            vote.id = id;
            render("WebVotes/edit.html", vote);
        }

        VoteQuestion.update(id, vote);
        index(vote.type);
    }

    public static void edit(Long id) {
        VoteQuestion vote = VoteQuestion.findById(id);
        render(vote);
    }

    private static void checkExpireAt(VoteQuestion question) {
        if (question.effectiveAt != null && question.expireAt != null && question.expireAt.before(question.effectiveAt)) {
            Validation.addError("vote.expireAt", "validation.beforeThanEffectiveAt");
        }
    }

    public static void delete(Long id) {
        VoteQuestion.delete(id);
        index(null);
    }
}
