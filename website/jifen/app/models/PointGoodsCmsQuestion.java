package models;

import models.cms.QuestionCondition;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-6
 * Time: 下午12:02
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "point_goods_user_question")
public class PointGoodsCmsQuestion extends Model {

    private static final long serialVersionUID = 81232409113062L;

    public String content;
    @Required
    public String reply;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "cookie_id")
    public String cookieId;

    @Column(name = "operate_user_id")
    public Long operateUserId;

    @Column(name = "point_goods_id")
    public Long pointGoodsId;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "replay_at")
    public Date repliedAt;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "remote_ip")
    public String remoteIP;

    @Column(name = "point_goods_name")
    public String pointGoodsName;

    public Boolean visible = true;

    @Transient
    public String operateUser;

    public PointGoodsCmsQuestion() {
        this.createdAt = new Date();
        this.userId = null;
        this.cookieId = null;
        this.userName = null;
    }

    public static JPAExtPaginator<PointGoodsCmsQuestion> getQuestionList(QuestionCondition condition, int pageNumber, int pageSize) {

        JPAExtPaginator<PointGoodsCmsQuestion> questions = new JPAExtPaginator<>("PointGoodsCmsQuestion q", "q",
                PointGoodsCmsQuestion.class, condition.getFitter(), condition.paramsMap).orderBy("createdAt desc");
        questions.setPageNumber(pageNumber);
        questions.setPageSize(pageSize);
        return questions;
    }

    public static List<PointGoodsCmsQuestion> findOnGoodsShow(Long userId, String cookieValue, Long pointGoodsId, int firstResult, int size) {
        if (firstResult < 0 || size < 0){
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("select q from PointGoodsCmsQuestion q where pointGoodsId = :pointGoodsId and ( (visible = :visible and reply IS NOT NULL) ");
        Map<String, Object> params = new HashMap<>();
        params.put("pointGoodsId", pointGoodsId);
        params.put("visible", true);

        if (userId != null) {
            sql.append("or userId = :userId ");
            params.put("userId", userId);
        }
        if (cookieValue != null) {
            sql.append("or cookieId = :cookieId");
            params.put("cookieId", cookieValue);
        }
        sql.append(") order by createdAt desc");

        Query query = PointGoodsCmsQuestion.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(firstResult);
        query.setMaxResults(firstResult + size);
        return query.getResultList();
    }


    public static void hide(long id) {
        PointGoodsCmsQuestion question = PointGoodsCmsQuestion.findById(id);
        question.visible = false;
        question.save();
    }

    public static void show(long id) {
        PointGoodsCmsQuestion question = PointGoodsCmsQuestion.findById(id);
        question.visible = true;
        question.save();
    }
}
