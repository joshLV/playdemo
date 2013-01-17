package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.WithdrawAccount;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import models.supplier.SupplierStatus;
import operate.rbac.RbacLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.data.validation.Error;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营后台商户的功能测试.
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

        SupplierCategory supplierCategory = FactoryBoy.create(SupplierCategory.class);
    }

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp");
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
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
    public void testAdd() {
        Http.Response response = GET(Router.reverse("Suppliers.add").url);
        assertIsOk(response);

        String baseDomain = (String) renderArgs("baseDomain");
        List<SupplierCategory> supplierCategoryList = (List<SupplierCategory>) renderArgs("supplierCategoryList");
        List<OperateUser> operateUserList = (List<OperateUser>) renderArgs("operateUserList");
        assertEquals(Play.configuration.getProperty("application.supplierDomain"), baseDomain);
        assertEquals(1, supplierCategoryList.size());
        assertEquals(1, operateUserList.size());
    }

    @Test
    public void testCreate_sucess() {
        assertEquals(1, Supplier.count());
        Map<String, String> params = new HashMap<>();
        params.put("supplier.domainName", "test-domain");
        params.put("supplier.fullName", "测试商户有限公司");
        params.put("supplier.otherName", "测试商户");
        params.put("supplier.position", "经理");
        params.put("supplier.userName", "王经理");
        params.put("supplier.loginName", "mrWang");
        params.put("supplier.mobile", "13212341234");
        params.put("admin.loginName", "sujie");
        params.put("admin.mobile", "13212341234");
        params.put("admin.jobNumber", "123456");
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/creative.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("image", vfImage.getRealFile());

        Http.Response response = POST(play.mvc.Router.reverse("Suppliers.create").url, params, fileParams);
        assertStatus(302, response);
        assertEquals(2, Supplier.count());
    }

    @Test
    public void testCreate_invalidDomain() {
        assertEquals(1, Supplier.count());
        Map<String, String> params = new HashMap<>();
        params.put("supplier.domainName", supplier.domainName);
        params.put("supplier.fullName", "测试商户有限公司");
        params.put("supplier.otherName", "测试商户");
        params.put("supplier.position", "经理");
        params.put("supplier.userName", "王经理");
        params.put("supplier.loginName", "mrWang");
        params.put("supplier.mobile", "13212341234");
        params.put("admin.loginName", "sujie");
        params.put("admin.mobile", "13212341234");
        params.put("admin.jobNumber", "123456");
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/creative.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("image", vfImage.getRealFile());

        Http.Response response = POST(play.mvc.Router.reverse("Suppliers.create").url, params, fileParams);
        assertIsOk(response);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("supplier.domainName", errors.get(0).getKey());
        assertEquals(1, Supplier.count());
    }

    @Test
    public void testCreate_invalidJobNumber() {
        assertEquals(1, Supplier.count());
        Map<String, String> params = new HashMap<>();
        params.put("supplier.domainName", "test-domain");
        params.put("supplier.fullName", "测试商户有限公司");
        params.put("supplier.otherName", "测试商户");
        params.put("supplier.position", "经理");
        params.put("supplier.userName", "王经理");
        params.put("supplier.loginName", "mrWang");
        params.put("supplier.mobile", "13212341234");
        params.put("admin.loginName", "sujie");
        params.put("admin.mobile", "13212341234");
        params.put("admin.jobNumber", "12345678");
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/creative.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("image", vfImage.getRealFile());

        Http.Response response = POST(play.mvc.Router.reverse("Suppliers.create").url, params, fileParams);
        assertIsOk(response);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("admin.jobNumber", errors.get(0).getKey());
        assertEquals(1, Supplier.count());
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

    @Test
    public void testWithdrawAccountCreateAndUpdate() {
        assertEquals(1, WithdrawAccount.count());

        Map<String, String> params = new HashMap<>();
        params.put("withdrawAccount.userName", "test-userName");
        params.put("withdrawAccount.bankCity", "test-bankCity");
        params.put("withdrawAccount.bankName", "test-bankName");
        params.put("withdrawAccount.subBankName", "test-subBankName");
        params.put("withdrawAccount.cardNumber", "test-cardNumber");
        params.put("supplierId", String.valueOf(supplier.id));

        Http.Response response = POST(Router.reverse("Suppliers.withdrawAccountCreateAndUpdate").url, params);
        assertStatus(302, response);

        assertEquals(2, WithdrawAccount.count());
    }

    @Test
    public void testWithdrawAccountDelete() {
        WithdrawAccount withdrawAccount = FactoryBoy.create(WithdrawAccount.class);
        assertEquals(2, WithdrawAccount.count());
        Long withdrawAccountId = withdrawAccount.id;
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(withdrawAccountId));
        params.put("supplierId", String.valueOf(supplier.id));
        Http.Response response = POST("/withdraw-account/delete", params);
        assertStatus(302, response);

        assertEquals(1, WithdrawAccount.count());
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(supplier.id));
        params.put("supplier.domainName", "test-domain");
        params.put("supplier.fullName", "test-fullName1");
        params.put("supplier.otherName", "test-otherName1");
        params.put("supplier.position", "manager1");
        params.put("supplier.userName", "managerWang");
        params.put("supplier.loginName", "mrWang");
        params.put("supplier.mobile", "13212341234");
        params.put("admin.loginName", "sujie");
        params.put("admin.mobile", "13212341234");
        params.put("admin.jobNumber", "123456");
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/creative.jpg");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("image", vfImage.getRealFile());

        Http.Response response = POST(play.mvc.Router.reverse("Suppliers.update").url, params, fileParams);
        assertStatus(302, response);
        supplier.refresh();
        assertEquals("test-domain", supplier.domainName);
        assertEquals("test-fullName1", supplier.fullName);
        assertEquals("test-otherName1", supplier.otherName);
        assertEquals("manager1", supplier.position);
        assertEquals("managerWang", supplier.userName);
    }

    @Test
    public void testShowCode() {

    }

    @Test
    public void testFreeze() {
        assertEquals(SupplierStatus.NORMAL, supplier.status);

        String params = "id=" + supplier.id;
        Http.Response response = PUT("/suppliers/" + supplier.id + "/freeze", "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        supplier.refresh();
        assertEquals(SupplierStatus.FREEZE, supplier.status);
    }

    @Test
    public void testUnfreeze() {
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();
        supplier.refresh();
        assertEquals(SupplierStatus.FREEZE, supplier.status);

        String params = "id=" + supplier.id;
        Http.Response response = PUT("/suppliers/" + supplier.id + "/unfreeze", "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        supplier.refresh();
        assertEquals(SupplierStatus.NORMAL, supplier.status);
    }

    @Test
    public void testDelete() {
        assertEquals(DeletedStatus.UN_DELETED, supplier.deleted);

        Http.Response response = DELETE("/suppliers/" + supplier.id);
        assertStatus(302, response);
        supplier.refresh();
        assertEquals(DeletedStatus.DELETED, supplier.deleted);
    }
}
