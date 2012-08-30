package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.AccountSequence;
import models.admin.OperateUser;
import models.consumer.User;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-27
 * Time: 下午1:50
 * To change this template use File | Settings | File Templates.
 */
public class WithdrawReportsFuncTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        final User u = FactoryBoy.create(User.class);
        // 初始化数据
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.tradeId = new Long(seq);
                target.account.uid = u.id;
            }
        });

    }

    @Test
    public void testIndex(){

        Http.Response response = GET("/reports/withdraw?condition.createdAtBegin=2012-01-01&condition.createdAtEnd=2099-08-24&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));

    }

    @Test
    public void testIndexWithNull(){
        Http.Response response = GET("/reports/withdraw");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Ignore
    @Test
    public void testDownload(){

        Http.Response response = GET("/reports/download/withdraw?condition.createdAtBegin=2012-01-01&condition.createdAtEnd=2099-08-24&condition.interval=");
        assertIsOk(response);
        //assertEquals("text/csv", response.contentType);

    }

}
