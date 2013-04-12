package functional.reports;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.VerifyCouponType;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;

public class OperateReportsTest extends FunctionalTest {

    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon ecoupon;
    SupplierUser supplierUser;

    Account supplierAccount;
    Account promenceAccount;
    Account uhuilaAccount;
    Account platformIncomingAccount;

    OperateUser operateUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);

        goods = FactoryBoy.create(Goods.class);
        ecoupon = FactoryBoy.create(ECoupon.class);

        //账户初始化
        supplierAccount = AccountUtil.getAccount(supplier.id, AccountType.SUPPLIER);
        supplierAccount.amount = new BigDecimal(100);
        supplierAccount.save();

        uhuilaAccount = AccountUtil.getUhuilaAccount();
        uhuilaAccount.amount = new BigDecimal(100);
        uhuilaAccount.save();

        promenceAccount = AccountUtil.getPromotionAccount();
        promenceAccount.amount = new BigDecimal(100);
        promenceAccount.save();

        platformIncomingAccount = AccountUtil.getPlatformIncomingAccount();
        platformIncomingAccount.amount = new BigDecimal(100);
        platformIncomingAccount.save();

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void 测试活动金报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/promotion");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(0, page.getRowCount());
    }

    @Test
    public void 测试财务收款报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/financing_incoming");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(0, page.getRowCount());
    }

    @Test
    public void 测试退款报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/withdraw");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(0, page.getRowCount());
    }

    @Test
    public void 测试收入账户报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/incoming");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(3, page.getRowCount());
    }

    @Test
    public void 测试平台佣金报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/platform");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(1, page.getRowCount());
    }

    @Test
    public void 测试网站佣金报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/website");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(1, page.getRowCount());
    }

    @Test
    public void 测试商户报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/supplier");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(1, page.getRowCount());
    }

    @Test
    public void 测试分销报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/resale");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(0, page.getRowCount());
    }

    @Test
    public void 测试消费者报表() throws Exception {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);

        Response response = GET("/reports/consumer");
        assertIsOk(response);
        JPAExtPaginator<AccountSequence> page = (JPAExtPaginator<AccountSequence>) renderArgs("accountSequencePage");
        assertEquals(0, page.getRowCount());
    }


}
