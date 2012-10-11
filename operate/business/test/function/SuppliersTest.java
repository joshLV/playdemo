package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.Play;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
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
    SupplierUser supplierUser;
    SupplierUser supplierUser2;
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
        supplierUser = FactoryBoy.create(SupplierUser.class);
        supplierUser2= FactoryBoy.create(SupplierUser.class);
        supplierUser2.supplierUserType =null;
        supplierUser2.save();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/suppliers");
        assertStatus(200, response);
        List<Supplier> supplierList = (List<Supplier>) renderArgs("suppliers");
        assertNotNull(supplierList);
        assertEquals(1, supplierList.size());
        assertEquals(supplier.fullName, supplierList.get(0).fullName);
    }

    @Test
    public void testExportMaterial() {
        Http.Response response = GET("/suppliers/export-material?supplierId=" + supplier.id + "&supplierDomainName=" + supplier.domainName);
        JPAExtPaginator<SupplierUser> supplierUserPageList = (JPAExtPaginator<SupplierUser>) renderArgs("supplierUsersPage");
        assertNotNull(supplierUserPageList);
        assertEquals(1, supplierUserPageList.size());
        assertEquals(supplierUser.mobile, supplierUserPageList.get(0).mobile);
        assertEquals(supplier.domainName, renderArgs("supplierDomainName"));

        JPAExtPaginator<SupplierUser> supplierUsersList = (JPAExtPaginator<SupplierUser>) renderArgs("supplierUsers");
        assertEquals(1, supplierUsersList.size());

    }


}
