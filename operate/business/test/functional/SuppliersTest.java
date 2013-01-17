package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.WithdrawAccount;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午1:53
 */
public class SuppliersTest extends FunctionalTest {
    Supplier supplier;
    SupplierUser supplierUser;
    SupplierUser supplierUser2;
    WithdrawAccount withdrawAccount;

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
        supplier.loginName = "tom";
        supplier.save();

        supplierUser = FactoryBoy.create(SupplierUser.class);
        supplierUser.loginName = "tom";
        supplierUser.supplier = supplier;
        supplierUser.save();

        supplierUser2 = FactoryBoy.create(SupplierUser.class);
        supplierUser2.supplierUserType = null;
        supplierUser2.save();

        withdrawAccount = FactoryBoy.create(WithdrawAccount.class);
        withdrawAccount.userId = supplier.id;
        withdrawAccount.userName = supplier.userName;
        withdrawAccount.save();
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
    public void testEdit() {
        Http.Response response = GET("/suppliers/" + supplier.id + "/edit");
        assertStatus(200, response);
        Supplier supplier = (Supplier) renderArgs("supplier");
        List<OperateUser> operateUserList = (List<OperateUser>) renderArgs("operateUserList");
        List<WithdrawAccount> withdrawAccounts = (List<WithdrawAccount>) renderArgs("withdrawAccounts");
        SupplierUser supplierUser = (SupplierUser) renderArgs("admin");
        String baseDomain = (String) renderArgs("baseDomain");
        assertNotNull(supplier);
        assertNotNull(supplierUser);
        assertNotNull(operateUserList);
        assertNotNull(withdrawAccounts);
        assertEquals(1, operateUserList.size());
        assertEquals("Supplier0", supplier.fullName);
        assertEquals("home.uhuila.net", baseDomain);
        assertEquals("tom", supplierUser.loginName);
        assertEquals(1, withdrawAccounts.size());
        assertEquals(supplier.id, withdrawAccounts.get(0).userId);
    }

    @Test
    public void testIndexByCondition() {
        Http.Response response = GET("/suppliers?otherName=supplier");
        assertStatus(200, response);
        List<Supplier> supplierList = (List<Supplier>) renderArgs("suppliers");
        assertNotNull(supplierList);
        assertEquals(1, supplierList.size());
        assertEquals("supplier", supplierList.get(0).otherName);
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
