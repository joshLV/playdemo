package function;

import java.util.List;

import models.operator.OperateUser;
import models.journal.WebServiceCallLog;

import operate.rbac.RbacLoader;

import org.junit.Before;
import org.junit.Test;

import controllers.operate.cas.Security;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class WebServiceCallLogsTest extends FunctionalTest {

    WebServiceCallLog log;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        log = FactoryBoy.create(WebServiceCallLog.class);
        FactoryBoy.create(WebServiceCallLog.class, new BuildCallback<WebServiceCallLog>() {
            @Override
            public void build(WebServiceCallLog log) {
                log.createdAt = DateHelper.beforeDays(1);
                log.duration = 3l;
            }
        });
    }
    
    @Test
    public void testIndex() throws Exception {
        assertEquals(2l, WebServiceCallLog.count());
        Response response = GET("/ws-logs");
        assertIsOk(response);
        
        JPAExtPaginator<WebServiceCallLog> logPage = (JPAExtPaginator<WebServiceCallLog>)renderArgs("logPage");
        assertEquals(1, logPage.getRowCount());  //默认只显示当天的日志
        List<WebServiceCallLog> logs = logPage.getCurrentPage();
        assertEquals(log.id, logs.get(0).id);
    }
    

    @Test
    public void testQueryType() throws Exception {
        Response response = GET("/ws-logs?log.callType=test");
        assertIsOk(response);
        
        JPAExtPaginator<WebServiceCallLog> logPage = (JPAExtPaginator<WebServiceCallLog>)renderArgs("logPage");
        assertEquals(1, logPage.getRowCount());  //默认只显示当天的日志
        List<WebServiceCallLog> logs = logPage.getCurrentPage();
        assertEquals(log.id, logs.get(0).id);
    }

    @Test
    public void testQueryNotExistType() throws Exception {
        Response response = GET("/ws-logs?log.callType=none");
        assertIsOk(response);
        
        JPAExtPaginator<WebServiceCallLog> logPage = (JPAExtPaginator<WebServiceCallLog>)renderArgs("logPage");
        assertEquals(0, logPage.getRowCount());  //默认只显示当天的日志
    }

    @Test
    public void testQueryKeyword() throws Exception {
        Response response = GET("/ws-logs?log.key1=abc");
        assertIsOk(response);
        
        JPAExtPaginator<WebServiceCallLog> logPage = (JPAExtPaginator<WebServiceCallLog>)renderArgs("logPage");
        assertEquals(1, logPage.getRowCount());  //默认只显示当天的日志
        List<WebServiceCallLog> logs = logPage.getCurrentPage();
        assertEquals(log.id, logs.get(0).id);
    }

    @Test
    public void testQueryNotExistKeyword() throws Exception {
        Response response = GET("/ws-logs?log.key1=none");
        assertIsOk(response);
        
        JPAExtPaginator<WebServiceCallLog> logPage = (JPAExtPaginator<WebServiceCallLog>)renderArgs("logPage");
        assertEquals(0, logPage.getRowCount());  //默认只显示当天的日志
    }
}
