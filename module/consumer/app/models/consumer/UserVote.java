package models.consumer;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-12
 * Time: 下午1:26
 */
@Entity
@Table(name = "user_vote")
public class UserVote extends Model {
    
    private static final long serialVersionUID = 812320609176823L;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public User user;
    @Mobile
    @Required
    public String mobile;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = true)
    public VoteQuestion vote;

    @Column(name = "created_at")
    public Date createdAt;

    public String answer;
    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public UserVote(User user, VoteQuestion vote, String answer, String mobile) {
        this.user = user;
        this.vote = vote;
        this.answer = answer;
        this.mobile = mobile;
        this.deleted = DeletedStatus.UN_DELETED;
        this.createdAt = new Date();

    }

    public static JPAExtPaginator getPage(int pageNumber, int pageSize, UserVoteCondition condition) {
        JPAExtPaginator<UserVote> votePage = new JPAExtPaginator<>(" UserVote u", "u",
                UserVote.class, condition.getCondition(), condition.paramsMap).orderBy("u.createdAt desc");

        votePage.setPageNumber(pageNumber);
        votePage.setPageSize(pageSize);
        return votePage;
    }


    public static boolean isVoted(User user,VoteQuestion vote) {
        List<UserVote> voteList = UserVote.find("user=? and vote=?", user, vote).fetch();
        return voteList.size() > 0;
    }
}
