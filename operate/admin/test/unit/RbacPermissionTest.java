package unit;

import java.util.List;

import models.admin.OperatePermission;

import org.junit.Ignore;
import org.junit.Test;

import play.Play;
import play.test.UnitTest;

public class RbacPermissionTest extends UnitTest {

    private String applicationName = Play.configuration.getProperty("application.name");

    @Test
    public void canLoadPermissionYamlFile() {
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
