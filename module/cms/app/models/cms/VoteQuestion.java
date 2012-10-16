package models.cms;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;


/**
 * <p/>
 * User: yanjy
 * Date: 12-6-11
 * Time: 下午2:26
 */
@Entity
@Table(name = "vote_question")
public class VoteQuestion extends Model {

    private static final long serialVersionUID = 8123206783262L;
   
    /**
     * 有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.DATE)
    public Date effectiveAt;

    /**
     * 有效结束日
     */
    @Required
    @Column(name = "expire_at")
    @Temporal(TemporalType.DATE)
    public Date expireAt;

    @Required
    @Column(name = "question_content")
    public String content;
    @Required
    public String answer1;
    @Required
    public String answer2;
    public String answer3;
    public String answer4;

    @Required
    @Column(name = "correct_answer")
    public String correctAnswer;

    @Enumerated(EnumType.STRING)
    public VoteType type;

    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public static ModelPaginator getPage(int pageNumber, int pageSize, VoteType type) {
        ModelPaginator<VoteQuestion> votePage;
        final String orderBy = " type, effectiveAt desc, expireAt";
        if (type == null) {
            votePage = new ModelPaginator<VoteQuestion>(VoteQuestion.class, "deleted = ?",
                    DeletedStatus.UN_DELETED).orderBy(orderBy);
        } else {
            votePage = new ModelPaginator<VoteQuestion>(VoteQuestion.class, "deleted = ? and type=?",
                    DeletedStatus.UN_DELETED, type).orderBy(orderBy);
        }
        votePage.setPageNumber(pageNumber);
        votePage.setPageSize(pageSize);
        return votePage;
    }

    public static List<VoteQuestion> getPage(VoteType type) {
        List<VoteQuestion> votesList = VoteQuestion.find("deleted = ? and type=? and effectiveAt <= ?" +
                " and expireAt >=? order by expireAt desc", DeletedStatus.UN_DELETED,type, DateUtil.getBeginOfDay(),
                DateUtil.getEndOfDay(new Date())).fetch();
        return votesList;
    }

    /**
     * 更新
     *
     * @param id   ID
     * @param vote 问卷调查内容
     */
    public static void update(Long id, VoteQuestion vote) {

        VoteQuestion newVote = VoteQuestion.findById(id);
        newVote.answer1 = vote.answer1;
        newVote.answer2 = vote.answer2;
        newVote.answer3 = vote.answer3;
        newVote.answer4 = vote.answer4;
        newVote.content = vote.content;
        newVote.effectiveAt = vote.effectiveAt;
        newVote.expireAt = vote.expireAt;
        newVote.type = vote.type;
        newVote.correctAnswer = vote.correctAnswer;
        newVote.save();
        

    }

    public static void delete(Long id) {
        VoteQuestion vote = VoteQuestion.findById(id);
        vote.deleted = DeletedStatus.DELETED;
        vote.save();
    }

    /**
     * 取得正确答案
     * @return
     */
    public String getAnswer() {
        String answer = "";
        if (answer1.contains(correctAnswer)) {
            answer = answer1;
        } else if (answer2.contains(correctAnswer)) {
            answer = answer2;
        } else if (StringUtils.isNotBlank(answer3) && answer3.contains(correctAnswer)) {
            answer = answer3;
        } else if (StringUtils.isNotBlank(answer4) && answer4.contains(correctAnswer)) {
            answer = answer4;
        }
        return answer;
    }
}
