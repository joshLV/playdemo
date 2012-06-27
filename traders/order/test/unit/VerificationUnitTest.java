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

    /**
     * 测试用户中心券列表
     */
    @Test
    public void testGetTimeRegion() {

        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(calendar.HOUR, -1);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(d);
        calendar1.add(calendar1.HOUR, 1);
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(id);
        String timeBegin = time.format(calendar.getTime());
        String timeEnd = time.format(calendar1.getTime());
        boolean timeFlag = eCoupon.getTimeRegion(timeBegin, timeEnd);
        assertTrue(timeFlag);

        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTime(d);
        calendar3.add(calendar3.HOUR, 1);

        Calendar calendar4 = Calendar.getInstance();
        calendar4.setTime(d);
        calendar4.add(calendar4.HOUR, 3);

        timeBegin = time.format(calendar3.getTime());
        timeEnd = time.format(calendar4.getTime());
        timeFlag = eCoupon.getTimeRegion(timeBegin, timeEnd);
        assertFalse(timeFlag);


        Calendar calendar5 = Calendar.getInstance();
        calendar5.setTime(d);
        calendar5.add(calendar5.DAY_OF_MONTH, 1);

        calendar4 = Calendar.getInstance();
        calendar4.setTime(d);
        calendar4.add(calendar4.HOUR, 3);

        timeBegin = time.format(calendar5.getTime());
        timeEnd = time.format(calendar4.getTime());
        timeFlag = eCoupon.getTimeRegion(timeBegin, timeEnd);
        assertFalse(timeFlag);

        Calendar calendar6 = Calendar.getInstance();
        calendar6.setTime(d);
        calendar6.add(calendar5.DAY_OF_MONTH, -1);

        calendar4 = Calendar.getInstance();
        calendar4.setTime(d);
        calendar4.add(calendar4.HOUR, 3);

        timeBegin = time.format(calendar5.getTime());
        timeEnd = time.format(calendar4.getTime());
        timeFlag = eCoupon.getTimeRegion(timeBegin, timeEnd);
        assertFalse(timeFlag);
    }

    @Test
    public void testUpdate() {
        String eCouponSn = "1234567002";
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        SupplierUser user = SupplierUser.find("supplier=?", new Supplier(supplierId)).first();
        ECoupon eCoupon = ECoupon.query(eCouponSn, supplierId);
        assertNotNull(eCoupon);

        Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_4");
        eCoupon.consumeAndPayCommission(shopId, user, VerifyCouponType.SHOP);
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
