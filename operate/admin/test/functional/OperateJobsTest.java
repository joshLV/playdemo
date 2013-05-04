package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.jobs.JobsDefine;
import models.jobs.JobsRunHistory;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-5-4
 * Time: 上午11:07
 */
public class OperateJobsTest extends FunctionalTest {

    JobsDefine jobsDefine;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        jobsDefine = FactoryBoy.create(JobsDefine.class);
        FactoryBoy.create(JobsRunHistory.class);
    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET(Router.reverse("OperateJobs.index").url);
        assertIsOk(response);
        JPAExtPaginator<JobsDefine> jobsPage = (JPAExtPaginator<JobsDefine>) renderArgs("jobsPage");
        assertEquals(1, jobsPage.getRowCount());
    }

    @Test
    public void testDetail() throws Exception {
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", jobsDefine.id);
        Http.Response response = GET(Router.reverse("OperateJobs.detail", urlParams).url);
        assertIsOk(response);
        JPAExtPaginator<JobsRunHistory> runHistoryPage = (JPAExtPaginator<JobsRunHistory>) renderArgs("runHistoryPage");
        assertEquals(1, runHistoryPage.getRowCount());
    }
}
