package functional;

import play.test.FunctionalTest;
import org.junit.Before;
import factory.FactoryBoy;
import play.vfs.VirtualFile;
import navigation.RbacLoader;
import models.admin.SupplierUser;
import controllers.supplier.cas.Security;
import org.junit.Test;
import play.mvc.Http;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-22
 * Time: 下午4:35
 * To change this template use File | Settings | File Templates.
 */
public class SupplierTotalSalesReportsFuncTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);



        SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }

    @Test //todo
    public void testTrends(){

        Http.Response response = GET("/totalsales/trends");
        assertStatus(302,response);
        //assertNotNull(renderArgs("dateList"));
        //assertNotNull(renderArgs("chartsMap"));
        //assertNotNull(renderArgs("reportPage"));

    }
}
