package models.cms;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-15
 * Time: 下午2:42
 */
@Entity
@Table(name = "user_question")
public class CmsQuestion extends Model {

    public String content;
    @Required
    public String reply;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "cookie_id")
    public String cookieId;

    @Column(name = "operate_user_id")
    public Long operateUserId;

    @Column(name = "goods_id")
    public Long goodsId;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "replay_at")
    public Date repliedAt;

    @Column(name = "user_name")
    public String userName;

    public Boolean visible = true;

    @Transient
    public String operateUser;

    public CmsQuestion() {
        this.createdAt = new Date();
        this.userId = null;
        this.cookieId = null;
        this.userName = null;
    }

    public static JPAExtPaginator<CmsQuestion> getQuestionList(QuestionCondition condition, int pageNumber, int pageSize) {

        JPAExtPaginator<CmsQuestion> questions = new JPAExtPaginator<>("CmsQuestion q", "q",
                CmsQuestion.class, condition.getFitter(), condition.paramsMap).orderBy("createdAt desc");
        questions.setPageNumber(pageNumber);
        questions.setPageSize(pageSize);
        return questions;
    }

    public static List<CmsQuestion> findOnGoodsShow(Long userId, String cookieValue, Long goodsId) {
        StringBuilder sql = new StringBuilder("select q from CmsQuestion q where goodsId = :goodsId and ( (visible = :visible and reply IS NOT NULL) ");
        Map<String, Object> params = new HashMap<>();
        params.put("goodsId", goodsId);
        params.put("visible", true);

        if (userId != null) {
            sql.append("or userId = :userId ");
            params.put("userId", userId);
        }
        if (cookieValue != null) {
            sql.append("or cookieId = :cookieId");
            params.put("cookieId", cookieValue);
        }
        sql.append(")");

        Query query = CmsQuestion.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }


    public static void hide(long id) {
        CmsQuestion question = CmsQuestion.findById(id);
        question.visible = false;
        question.save();
    }

    public static void show(long id) {
        CmsQuestion question = CmsQuestion.findById(id);
        question.visible = true;
        question.save();
    }

}
