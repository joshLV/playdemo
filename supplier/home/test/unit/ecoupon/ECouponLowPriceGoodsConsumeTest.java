package unit.ecoupon;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.VerifyCouponType;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;

/**
 * 验证售价低于进价的产品.
 * @author tanglq
 */
public class ECouponLowPriceGoodsConsumeTest extends UnitTest {

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
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.salePrice = BigDecimal.TEN;
                g.originalPrice = new BigDecimal(20);
                g.resaleAddPrice = BigDecimal.ZERO;
            }
        });
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
    }
    
    @Test
    public void 测试一百券销售的低价产品() throws Exception {
//        public boolean consumeAndPayCommission(Long shopId, Long operateUserId,
//                        SupplierUser supplierUser, VerifyCouponType type) {
        Boolean success = ecoupon.consumeAndPayCommission(shop.id, supplierUser, VerifyCouponType.TELEPHONE);
        assertTrue(success);
        
        // 平台收款帐户减少10元
        platformIncomingAccount.refresh();
        assertEquals(new BigDecimal("90.00"), platformIncomingAccount.amount.setScale(2));
        // 活动收款帐户减少10元
        promenceAccount.refresh();
        assertEquals(new BigDecimal("90.00"), promenceAccount.amount.setScale(2));
        // 商户收到20元
        supplierAccount.refresh();
        assertEquals(new BigDecimal("120.00"), supplierAccount.amount.setScale(2));
        // 一百券佣金没有变化
        uhuilaAccount.refresh();
        assertEquals(new BigDecimal("100.00"), uhuilaAccount.amount.setScale(2));
    }
}
