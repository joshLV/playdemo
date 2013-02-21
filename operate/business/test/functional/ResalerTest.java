package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.admin.SupplierUser;
import models.resale.AccountType;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

public class ResalerTest extends FunctionalTest {
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(SupplierUser.class);
        FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.accountType = AccountType.CONSUMER;
            }
        });


        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
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
        assertEquals(1, ((JPAExtPaginator<Resaler>) renderArgs("resalers")).size());
    }

    /**
     * 查看分销商详细信息
     */
    @Test
    public void testDetails() {
        Resaler resaler = FactoryBoy.last(Resaler.class);
        Response response = GET("/resalers/" + resaler.id + "/view");
        assertStatus(200, response);
        assertContentMatch(resaler.loginName, response);
        assertEquals(resaler.id, ((Resaler) renderArgs("resaler")).id);
    }

    /**
     * 审核分销商
     */
    @Test
    public void testUpdate() {
        Resaler resaler = FactoryBoy.last(Resaler.class);
        assertEquals(ResalerStatus.APPROVED, resaler.status);
        assertNull(resaler.remark);

        String remark = "unapproved";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", resaler.id.toString());
        paramMap.put("status", ResalerStatus.UNAPPROVED.toString());
        paramMap.put("remark", remark);
        Response response = POST("/resalers/check", paramMap);
        assertStatus(302, response);

        resaler.refresh();
        assertEquals(ResalerStatus.UNAPPROVED, resaler.status);
        assertEquals(remark, resaler.remark);
    }

    @Test
    public void testFreeze() {
        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.accountType = AccountType.CONSUMER;
                target.status = ResalerStatus.APPROVED;
            }
        });
        assertEquals(ResalerStatus.APPROVED, resaler.status);
        Http.Response response = PUT("/resalers/" + resaler.id + "/freeze", "text/html", "");
        assertStatus(302, response);
        resaler.refresh();
        assertEquals(ResalerStatus.FREEZE, resaler.status);
    }

    @Test
    public void testUnFreeze() {
        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.accountType = AccountType.CONSUMER;
                target.status = ResalerStatus.FREEZE;
            }
        });
        assertEquals(ResalerStatus.FREEZE, resaler.status);
        Http.Response response = PUT("/resalers/" + resaler.id + "/unfreeze", "text/html", "");
        assertStatus(302, response);
        resaler.refresh();
        assertEquals(ResalerStatus.APPROVED, resaler.status);
    }


}
