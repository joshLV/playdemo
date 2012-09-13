package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
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
    public String scheduledTime;

    /**
     * 已完成
     */
    @Column(name = "finished")
    public Long finished;

    /**
     * 未完成
     */
    @Column(name = "unfinished")
    public Long unfinished;

    /**
     * 总任务数
     */
    @Column(name = "total")
    public Long total;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;


    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;



    public static JPAExtPaginator<SendSMSTask> findByCondition(SendSMSTaskCondition condition,
                                                               int pageNumber, int pageSize) {

        JPAExtPaginator<SendSMSTask> smsTaskList = new JPAExtPaginator<>
                ("SendSMSTask st", "st", SendSMSTask.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        smsTaskList.setPageNumber(pageNumber);
        smsTaskList.setPageSize(pageSize);
        smsTaskList.setBoundaryControlsEnabled(false);
        return smsTaskList;

    }
}
