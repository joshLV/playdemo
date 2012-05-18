package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author likang
 * Date: 12-5-18
 */
@Entity
@Table(name = "mq_test_journal")
public class MQTestJournal extends Model {
    public String message;
    public Date createdAt;

    public MQTestJournal(String message){
        this.message = message;
        this.createdAt = new Date();
    }
}
