package functional.finance;

import models.accounts.WithdrawBill;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;
import factory.FactoryBoy;

public class WithdrawApprovalTest extends FunctionalTest {

    WithdrawBill withdrawBill;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        
        FactoryBoy.create(WithdrawBill.class);
    }
    
    @Test
    public void testIndex() throws Exception {
        Response response = GET("/withdraw");
        assertIsOk(response);
    }
}
