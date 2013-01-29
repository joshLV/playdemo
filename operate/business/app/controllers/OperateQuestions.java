package controllers;

import models.admin.OperateUser;
import models.cms.CmsQuestion;
import models.cms.GoodsType;
import models.cms.QuestionCondition;
import models.sales.Goods;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-15
 * Time: 下午4:31
 */


@With(OperateRbac.class)
@ActiveNavigation("questions_index")
public class OperateQuestions extends Controller {
    private static final int PAGE_SIZE = 15;

    @ActiveNavigation("questions_index")
    public static void index(QuestionCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new QuestionCondition();
        }
        JPAExtPaginator<CmsQuestion> questions = CmsQuestion.getQuestionList(condition, pageNumber, PAGE_SIZE);

        for (CmsQuestion question : questions.getCurrentPage()) {
            setItems(question);
        }
        render(questions, condition);
    }


    public static void update(Long id, @Valid CmsQuestion question) {
        OperateUser operateUser = OperateRbac.currentUser();
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            setItems(question);
            render("/OperateQuestions/reply.html", question, operateUser);
        }

        CmsQuestion updateQuestion = CmsQuestion.findById(id);
        updateQuestion.repliedAt = new Date();
        updateQuestion.operateUserId = operateUser.id;
        updateQuestion.reply = question.reply;
        updateQuestion.save();
        index(null);
    }

    public static void edit(Long id) {
        CmsQuestion question = CmsQuestion.findById(id);
        setItems(question);
        OperateUser operateUser = OperateRbac.currentUser();
        render("/OperateQuestions/reply.html", question, operateUser);
    }

    private static void setItems(CmsQuestion question) {
        if (question.userId == null) {
            question.userName = "游客";
        }
        if (question.operateUserId != null) {
            OperateUser operateUser = OperateUser.findById(question.operateUserId);
            if (operateUser != null) {
                question.operateUser = operateUser.userName;
            }
        }

        if (question.goodsId != null) {
            if (question.goodsType == GoodsType.POINTGOODS) {
                models.sales.PointGoods pointGoods = models.sales.PointGoods.findById(question.goodsId);
                question.goodsName = pointGoods.name;
            } else {
                Goods goods = Goods.findById(question.goodsId);
                question.goodsName = goods.name;
            }
        }
        question.save();
    }

    public static void delete(long id) {
        CmsQuestion question = CmsQuestion.findById(id);
        question.delete();

        index(null);
    }


    public static void hide(long id) {
        CmsQuestion.hide(id);
        index(null);
    }

    public static void show(long id) {
        CmsQuestion.show(id);
        index(null);
    }

}
