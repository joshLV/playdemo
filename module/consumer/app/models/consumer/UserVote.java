package models.consumer;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.*;
import java.util.Date;

/**
 * TODO.
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

    public static ModelPaginator getPage(int pageNumber, int pageSize,VoteType type) {

        ModelPaginator<UserVote> votePage;
        votePage = new ModelPaginator<UserVote>(UserVote.class, "deleted = ? ",
                DeletedStatus.UN_DELETED).orderBy("createdAt desc");

        votePage.setPageNumber(pageNumber);
        votePage.setPageSize(pageSize);
        return votePage;
    }

}
