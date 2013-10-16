package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.ConsultRecord;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * User: wangjia
 * Date: 12-9-25
 * Time: 上午10:57
 */
public class OperateConsultTest extends FunctionalTest {
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
    public void indexTest() {
        Http.Response response = GET("/callcenter/search");
        assertStatus(200, response);
        assertContentMatch("消费者信息管理系统查询", response);
    }

    @Test
    @Ignore
    public void consultExcelOutTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = GET("/consult_excel_out");
        assertStatus(200, response);
        JPAExtPaginator<ConsultRecord> consultList = (JPAExtPaginator<ConsultRecord>) renderArgs("consultList");
        assertNotNull(consultList);
        assertEquals(1, consultList.size());
        assertEquals("订购咨询", consultList.get(0).consultTypeInfo);
    }


}
