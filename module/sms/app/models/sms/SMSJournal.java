package models.sms;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User: likang
 */

@Entity
@Table(name = "sms_journal")
public class SMSJournal extends Model {
    public String content;
    public String phone;
    public String status;
    public String serial;
    
    public SMSJournal(String content, String phone, String status, String serial){
        this.content = content;
        this.phone = phone;
        this.status = status;
        this.serial = serial;
    }
}
