package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-3
 * Time: 上午11:26
 * To change this template use File | Settings | File Templates.
 */
public class SuppliersCategoryTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {

    }
}
