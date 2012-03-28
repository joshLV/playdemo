package unit;

import java.util.List;

import models.admin.OperateRole;

import org.junit.Test;

import play.test.UnitTest;

public class RbacRoleTest extends UnitTest {

    @Test
    public void canLoadRoleToDB() {
        // 加载后，数据库中必须有相关的记录
        List<OperateRole> roles = OperateRole.findAll();
        assertTrue(roles.size() > 0);

        OperateRole adminRole = OperateRole.find("byKey", "admin")
                .first();
        assertNotNull(adminRole);
        assertEquals("系统管理员", adminRole.text);
    }

}
