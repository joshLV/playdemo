package unit;

import factory.FactoryBoy;
import models.operator.OperateNavigation;
import models.operator.OperatePermission;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.List;

public class RbacPermissionTest extends UnitTest {

    private String applicationName = Play.configuration.getProperty("application.name");

    @Before
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(OperateNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void canLoadPermissionFile() {
        // 加载后，数据库中必须有相关的记录
        List<OperatePermission> perms = OperatePermission.find("byApplicationName", applicationName).fetch();
        assertTrue(perms.size() > 0);

        OperatePermission user = OperatePermission.find("byApplicationNameAndKey", applicationName, "USER")
                .first();
        assertNotNull(user);
        OperatePermission userAdd = OperatePermission.find("byApplicationNameAndKey", applicationName, "USER_ADD")
                .first();
        assertNotNull(userAdd);
        assertNotNull(userAdd.parent);
        assertEquals(user.text, userAdd.parent.text);

    }


}
