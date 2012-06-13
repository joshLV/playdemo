package models.cms;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-11
 * Time: 下午2:51
 */

@Entity
@Table(name = "vote_answer")
public class VoteAnswer extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = true)
    public VoteQuestion vote;
    public String answer;

    public VoteAnswer(VoteQuestion vote, String answer) {
        this.vote = vote;
        this.answer = answer;
    }
}
