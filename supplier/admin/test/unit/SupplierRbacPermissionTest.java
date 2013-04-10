package unit;

import factory.FactoryBoy;
import models.admin.SupplierPermission;
import models.operator.OperateNavigation;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.List;

public class SupplierRbacPermissionTest extends UnitTest {


    @Before
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(OperateNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void canLoadPermissionYamlFile() {
        // 加载后，数据库中必须有相关的记录
        List<SupplierPermission> perms = SupplierPermission.find("byApplicationName", "traders-admin").fetch();
        assertTrue(perms.size() > 0);

        SupplierPermission user = SupplierPermission.find("byApplicationNameAndKey", "traders-admin", "USER")
                .first();
        assertNotNull(user);
        SupplierPermission userAdd = SupplierPermission.find("byApplicationNameAndKey", "traders-admin", "USER_ADD")
                .first();
        assertNotNull(userAdd);
        assertNotNull(userAdd.parent);
        assertEquals(user.text, userAdd.parent.text);

    }

}
