package unit;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.*;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VerificationUnitTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(Account.class);
        Fixtures.delete(SupplierRole.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.loadModels("fixture/goods_base.yml", "fixture/roles.yml",
                "fixture/supplierusers.yml",
                "fixture/user.yml",
                "fixture/goods.yml", "fixture/accounts.yml",
                "fixture/orders.yml",
                "fixture/orderItems.yml");

        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        List<Goods> goodsList = Goods.findAll();
        for (Goods goods : goodsList) {
            goods.supplierId = supplierId;
            goods.save();
        }

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();

    }

    @After
    public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void testUpdate() {
        String eCouponSn = "1234567002";
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        SupplierUser user = SupplierUser.find("supplier=?", new Supplier(supplierId)).first();
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        assertNotNull(eCoupon);

        Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_4");
        eCoupon.consumeAndPayCommission(shopId, null, user, VerifyCouponType.SHOP);
        List<ECoupon> couponList = ECoupon.find("byECouponSn", eCouponSn).fetch();
        assertEquals(ECouponStatus.CONSUMED, couponList.get(0).status);
        assertEquals(shopId, couponList.get(0).shop.id);
    }


    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons() {
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        int pageNumber = 1;
        int pageSize = 15;
        List<ECoupon> list = ECoupon.queryCoupons(supplierId, pageNumber, pageSize);
        assertEquals(4, list.size());

    }

    @Test
    public void testGetConsumedShop() {
        String eCouponSn = "1234567004";
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        String name = eCoupon.getConsumedShop();
        assertEquals("优惠拉", name);

    }

}
