package models.cms;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
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

    public Boolean visible = true;

    @Transient
    public String user;
    @Transient
    public String operateUser;

    public CmsQuestion() {
        this.createdAt = new Date();
        this.userId = null;
        this.cookieId = null;
    }


    public static JPAExtPaginator<CmsQuestion> getQuestionList(int pageNumber, int pageSize) {
        StringBuilder sql = new StringBuilder("1=1");
        Map params = new HashMap();
        sql.append("and visible =:visible");
        params.put("visible", true);
        JPAExtPaginator<CmsQuestion> questions = new JPAExtPaginator<>("CmsQuestion q", "q",
                CmsQuestion.class, sql.toString(), params).orderBy("createdAt desc");
        questions.setPageNumber(pageNumber);
        questions.setPageSize(pageSize);
        return questions;

    }
}
