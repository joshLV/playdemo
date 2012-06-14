package models.consumer;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-12
 * Time: 下午1:26
 */
@Entity
@Table(name = "user_vote")
public class UserVote extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = true)
    public VoteQuestion vote;

    @Column(name = "created_at")
    public Date createdAt;

    public String answer;
    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public UserVote(User user, VoteQuestion vote, String answer) {
        this.user = user;
        this.vote = vote;
        this.answer = answer;
        this.createdAt = new Date();

    }

    public static JPAExtPaginator getPage(int pageNumber, int pageSize, UserVoteCondition condition) {
        JPAExtPaginator<UserVote> votePage = new JPAExtPaginator<>("UserVote u", "u",
                UserVote.class, condition.getCondition(), condition.paramsMap).orderBy("u.createdAt desc");

        votePage.setPageNumber(pageNumber);
        votePage.setPageSize(pageSize);
        return votePage;
    }

}
