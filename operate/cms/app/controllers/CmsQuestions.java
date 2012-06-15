package controllers;

import models.cms.CmsQuestion;
import operate.rbac.annotations.ActiveNavigation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import org.apache.commons.lang.StringUtils;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-15
 * Time: 下午2:35
 */
@With(OperateRbac.class)
@ActiveNavigation("question_index")
public class CmsQuestions extends Controller {
    private static final int PAGE_SIZE = 15;

    public static void index() {

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<CmsQuestion> questions = CmsQuestion.getQuestionList(pageNumber, PAGE_SIZE);

        render(questions);
    }


    public static void update(Long id) {


        index();
    }
    @ActiveNavigation("question_edit")
    public static void edit(Long id) {
        render();
    }


    public static void delete(Long id) {
        index();
    }
}
