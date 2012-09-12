package models.sales;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-12
 * Time: 下午4:01
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "send_sms_task")
public class SendSMSTask  extends Model {
    /**
     * 任务号
     */
    @Column(name = "task_no")
    public String taskNo;

    /**
     * 计划发送时间
     */
    @Column(name = "scheduled_time")
    public Date scheduledTime;




}
