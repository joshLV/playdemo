package models.consumer;

import models.cms.VoteQuestion;
import play.db.jpa.Model;

import javax.persistence.*;

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

    public String answer;

    public UserVote(User user, VoteQuestion vote, String answer) {
        this.user = user;
        this.vote = vote;
        this.answer = answer;
    }
}
