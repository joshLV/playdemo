package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午1:53
 * To change this template use File | Settings | File Templates.
 */
public class SuppliersTest extends FunctionalTest {
    Supplier supplier;

    @org.junit.Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        supplier = FactoryBoy.create(Supplier.class);
    }

    @Test
    public void testIndex() {

    }
}
