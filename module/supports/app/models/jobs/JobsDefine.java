package models.jobs;

import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @ManyToOne
    @JoinColumn(name="last_run_history_id")
    public JobsRunHistory lastRunHistory;

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

    public static JPAExtPaginator<JobsDefine> query(JobsDefine jobs, int pageNumber, int pageSize) {
        StringBuilder sql = new StringBuilder("1=1");
        Map params = new HashMap();

        if (StringUtils.isNotBlank(jobs.className)) {
            sql.append(" and l.className like :className");
            params.put("className", "%" + jobs.className + "%");
        }
        if (StringUtils.isNotBlank(jobs.title)) {
            sql.append(" and l.title like :title");
            params.put("title", "%" + jobs.title + "%");
        }

        JPAExtPaginator<JobsDefine> jobsPage = new JPAExtPaginator<>(
                "JobsDefine l", "l",
                JobsDefine.class, sql.toString(), params)
                .orderBy("l.lastRunHistory desc");
        jobsPage.setPageNumber(pageNumber);
        jobsPage.setPageSize(pageSize);
        jobsPage.setBoundaryControlsEnabled(true);
        return jobsPage;
    }
}
