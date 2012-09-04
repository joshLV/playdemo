package function;

import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

public class ResalerTest extends FunctionalTest {


    @org.junit.Before
    public void setup() {
        Fixtures.delete(Resaler.class);

        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplierusers.yml");

        Fixtures.loadModels("fixture/resaler.yml");

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    /**
     * 查看分销商信息
     */
    @Test
    public void testIndex() {
        Response response = GET("/resalers");
        assertStatus(200, response);
    }

    /**
     * 查看分销商详细信息
     */
    @Test
    public void testDetails() {
        Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_1");
        Response response = GET("/resalers/" + id + "/view");
        assertStatus(200, response);
        Resaler resaler = Resaler.findById(id);
        assertEquals(AccountType.CONSUMER, resaler.accountType);
        assertEquals("yanjy", resaler.loginName);
    }

    /**
     * 审核分销商
     */
    @Test
    public void testUpdate() {
        Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_2");
        String remark = "approved";
        Map<String, String> paramMap = new HashMap();
        paramMap.put("id", id.toString());
        paramMap.put("status", ResalerStatus.APPROVED.toString());
        paramMap.put("level", ResalerLevel.NORMAL.toString());
        paramMap.put("remark", remark);
        Response response = POST("/resalers/update", paramMap);
        assertStatus(302, response);
        Resaler resaler = Resaler.findById(id);
        assertEquals(ResalerStatus.APPROVED, resaler.status);
        assertEquals(ResalerLevel.NORMAL, resaler.level);
        assertEquals(remark, resaler.remark);

        id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_4");
        remark = "unapproved";
        paramMap = new HashMap();
        paramMap.put("id", id.toString());
        paramMap.put("status", ResalerStatus.UNAPPROVED.toString());
        paramMap.put("level", ResalerLevel.NORMAL.toString());
        paramMap.put("remark", remark);
        response = POST("/resalers/update", paramMap);
        assertStatus(302, response);
        resaler = Resaler.findById(id);
        assertEquals(ResalerStatus.UNAPPROVED, resaler.status);
        assertEquals(remark, resaler.remark);

    }

}
