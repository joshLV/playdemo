package unit;

import factory.FactoryBoy;
import models.admin.SupplierPermission;
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
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void canLoadPermissionYamlFile() {
        // 加载后，数据库中必须有相关的记录
        List<SupplierPermission> perms = SupplierPermission.find("byApplicationName", "traders-home").fetch();
        assertTrue(perms.size() > 0);

        SupplierPermission user = SupplierPermission.find("byApplicationNameAndKey", "traders-home", "USER")
                .first();
        assertNotNull(user);
        SupplierPermission userAdd = SupplierPermission.find("byApplicationNameAndKey", "traders-home", "USER_ADD")
                .first();
        assertNotNull(userAdd);
        assertNotNull(userAdd.parent);
        assertEquals(user.text, userAdd.parent.text);

    }

}
