package models.journal;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.Model;

/**
 * User: likang
 */

@Entity
@Table(name = "mq_journal")
public class MQJournal extends Model {
    @Column(name = "queue_name")
    public String queueName;

    @Column(length=4000)
    public String journal;
    
    @Column(name = "created_at")
    public Date createdAt;
    
    public MQJournal(String queueName, String journal){
        this.queueName = queueName;
        this.journal = journal;
        this.createdAt = new Date();
    }
}
