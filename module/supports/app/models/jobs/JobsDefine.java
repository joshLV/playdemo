package models.jobs;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 消息队列定义.
 *
 */
@Entity
@Table(name="jobs_defines")
public class JobsDefine extends Model {

    @Column(name="class_name", unique = true)
    public String className;

    @Column
    public String title;

    @Column
    public String description;

    @Column
    public String scheduledInfo;

    public static JobsDefine forClass(String className) {
        JobsDefine define = new JobsDefine();
        define.className = className;
        return define;
    }

    public JobsDefine title(String _title) {
        this.title = _title;
        return this;
    }

    public JobsDefine description(String _desc) {
        this.description = _desc;
        return this;
    }

    public JobsDefine scheduled(String _scheduledInfo) {
        this.scheduledInfo = _scheduledInfo;
        return this;
    }

    /**
     * 加载或保存JobsDefine.
     * @return
     */
    public JobsDefine load() {
        JobsDefine jobsDefine = JobsDefine.find("className=? order by id desc", this.className).first();

        if (jobsDefine != null) {
            jobsDefine.title = this.title;
            jobsDefine.description = this.description;
            jobsDefine.scheduledInfo = this.scheduledInfo;
            jobsDefine.save();
            return jobsDefine;
        }
        this.save();
        return this;
    }

    public void runAt(Date runnedAt, String status, String remark) {

    }
}
