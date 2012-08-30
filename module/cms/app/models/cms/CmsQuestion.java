package models.cms;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.*;
import models.cms.GoodsType;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-15
 * Time: 下午2:42
 */
@Entity
@Table(name = "user_question")
public class CmsQuestion extends Model {

    private static final long serialVersionUID = 81232409113062L;
    
    public String content;

    public String mobile;

    @Required
    public String reply;

    @Column(name = "user_id")
    public Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goods_type")
    public GoodsType goodsType;

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

    @Column(name = "remote_ip")
    public String remoteIP;

    @Column(name = "goods_name")
    public String goodsName;

    public Boolean visible = true;

    @Transient
    public String operateUser;

    public CmsQuestion() {
        this.createdAt = new Date();
        this.mobile = "";
        this.userId = null;
        this.cookieId = null;
        this.userName = null;
        this.goodsType = GoodsType.NORMALGOODS;
    }

    public static JPAExtPaginator<CmsQuestion> getQuestionList(QuestionCondition condition, int pageNumber, int pageSize) {

        JPAExtPaginator<CmsQuestion> questions = new JPAExtPaginator<>("CmsQuestion q", "q",
                CmsQuestion.class, condition.getFitter(), condition.paramsMap).orderBy("createdAt desc");
        questions.setPageNumber(pageNumber);
        questions.setPageSize(pageSize);
        return questions;
    }

    public static List<CmsQuestion> findOnGoodsShow(Long userId, String cookieValue, Long goodsId, GoodsType goodsType, int firstResult, int size) {
        if (firstResult < 0 || size < 0){
            return new ArrayList<>();
        }
        if (goodsType == null){
            goodsType = GoodsType.NORMALGOODS;
        }

        StringBuilder sql;
        Map<String, Object> params = new HashMap<>();

        if (goodsType == GoodsType.NORMALGOODS){
            sql = new StringBuilder("select q from CmsQuestion q where goodsId = :goodsId and ((goodsType = :goodsType) OR (goodsType IS NULL)) and ( (visible = :visible and reply IS NOT NULL) ");
        }
        else{
            sql = new StringBuilder("select q from CmsQuestion q where goodsId = :goodsId and goodsType = :goodsType and ( (visible = :visible and reply IS NOT NULL) ");
        }

        params.put("goodsId", goodsId);
        params.put("goodsType", goodsType);
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

        Query query = CmsQuestion.em().createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(firstResult);
        query.setMaxResults(firstResult + size);
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
