package controllers;

import models.jobs.JobsDefine;
import models.jobs.JobsRunHistory;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 查看Jobs运行情况.
 */
@With(OperateRbac.class)
@ActiveNavigation("jobs_view")
public class OperateJobs extends Controller {

    /**
     * 列出所有JobsDefine.
     */
    public static void index(JobsDefine jobs) {

        if (jobs == null) {
            jobs = new JobsDefine();
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<JobsDefine> jobsPage = JobsDefine.query(jobs,
                pageNumber, 30);

        render(jobs, jobsPage);
    }

    /**
     * 列出指定Jobs的执行历史.
     * @param id
     */
    public static void detail(Long id) {
        JobsDefine jobsDefine = JobsDefine.findById(id);
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<JobsRunHistory> runHistoryPage = JobsRunHistory.query(jobsDefine, pageNumber, 30);

        render(jobsDefine, runHistoryPage);
    }
}
