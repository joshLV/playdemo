package unit;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.List;

public class VerificationUnitTest extends UnitTest {
    
    Supplier supplier;
    Shop shop;
    SupplierUser supplierUser;
    Goods goods;
    ECoupon ecoupon;
    
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        
        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        goods = FactoryBoy.create(Goods.class);
        ecoupon = FactoryBoy.create(ECoupon.class);

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
        String eCouponSn = ecoupon.eCouponSn;
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplier.id);
        assertNotNull(eCoupon);

        eCoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.SHOP);
        List<ECoupon> couponList = ECoupon.find("byECouponSn", eCouponSn).fetch();
        assertEquals(ECouponStatus.CONSUMED, couponList.get(0).status);
        assertEquals(shop.id, couponList.get(0).shop.id);
    }


    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons() {
        int pageNumber = 1;
        int pageSize = 15;
        List<ECoupon> list = ECoupon.queryCoupons(supplier.id, pageNumber, pageSize);
        assertEquals(1, list.size());

    }

    @Test
    public void testGetConsumedShop() {
        assertEquals("", ecoupon.getConsumedShop());
        ecoupon.shop = shop;
        ecoupon.save();
        String eCouponSn = ecoupon.eCouponSn;
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplier.id);
        String name = eCoupon.getConsumedShop();
        assertEquals(shop.name, name);

    }

}
