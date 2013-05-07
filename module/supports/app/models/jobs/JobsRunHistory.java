package models.jobs;

import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import util.DateHelper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Jobs运行历史.
 */
@Entity
@Table(name="jobs_run_histories")
public class JobsRunHistory extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobs_define_id", nullable = true)
    public JobsDefine jobsDefine;

    @Column(name="runned_at")
    public Date runnedAt;

    public String status;

    public String remark;

    /**
     * 运行时长，毫秒.
     */
    public long runTimes;

    public static JPAExtPaginator<JobsRunHistory> query(JobsDefine jobsDefine, int pageNumber, int pageSize) {
        StringBuilder sql = new StringBuilder(" l.jobsDefine.id = :jobsDefine ");
        Map params = new HashMap();
        params.put("jobsDefine", jobsDefine.id);

        JPAExtPaginator<JobsRunHistory> jobsRunHistoryPage = new JPAExtPaginator<>(
                "JobsRunHistory l", "l",
                JobsRunHistory.class, sql.toString(), params)
                .orderBy("l.id desc");
        jobsRunHistoryPage.setPageNumber(pageNumber);
        jobsRunHistoryPage.setPageSize(pageSize);
        jobsRunHistoryPage.setBoundaryControlsEnabled(true);
        return jobsRunHistoryPage;
    }

    public static void deleteBeforeItem(JobsDefine jobsDefine, int retainHistoryMinutes) {
        JobsRunHistory.delete("jobsDefine=? and runnedAt<?", jobsDefine,
                DateHelper.beforeMinuts(retainHistoryMinutes));
    }

}
