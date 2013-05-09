package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.order.BatchCoupons;
import models.order.ECoupon;
import models.resale.Resaler;
import models.resale.ResalerBatchExportCoupons;
import models.resale.ResalerFav;
import models.sales.Goods;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-23
 * Time: 下午3:21
 */
public class BatchExportCouponsTest extends FunctionalTest {
    Goods goods;
    Account account;
    ECoupon coupon;
    BatchCoupons batchCoupons;
    Resaler resaler;
    ResalerFav resalerFav;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler r) {
                r.batchExportCoupons = ResalerBatchExportCoupons.YES;
            }
        });
        resalerFav = FactoryBoy.create(ResalerFav.class);

        batchCoupons = FactoryBoy.create(BatchCoupons.class);
        coupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.batchCoupons = batchCoupons;
            }
        });
        account = FactoryBoy.create(Account.class, new BuildCallback<Account>() {
            @Override
            public void build(Account a) {
                a.uid = resaler.id;
                a.accountType = AccountType.RESALER;
                a.amount = BigDecimal.valueOf(1000000);
            }
        });


        Security.setLoginUserForTest(resaler.loginName);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/coupons/batchexport/index");
        assertIsOk(response);
        JPAExtPaginator<BatchCoupons> couponPage = (JPAExtPaginator<BatchCoupons>) renderArgs("couponPage");
        assertNotNull(couponPage);
        assertEquals(1, couponPage.size());
    }

    @Test
    public void testGenerator() {
        Http.Response response = GET("/coupons/batchexport/generator");
        assertIsOk(response);
        assertEquals(1, ((List<Goods>) renderArgs("goodsList")).size());
    }


    @Test
    public void testGenerate() {
        Map<String, String> params = new HashMap<>();
        params.put("count", "1");
        params.put("name", "test");
        params.put("prefix", "12");
        params.put("goodsId", goods.id.toString());
        params.put("consumed", goods.salePrice.toString());
        Http.Response response = POST("/coupons/batchexport/generate", params);
        assertStatus(302, response);
        assertEquals(2, BatchCoupons.count());
    }

    @Test
    public void testDetails() {
        Http.Response response = GET("/coupons/batchexport/" + batchCoupons.id);
        assertIsOk(response);
        JPAExtPaginator<BatchCoupons> couponPage = (JPAExtPaginator<BatchCoupons>) renderArgs("couponPage");
        assertEquals(1, couponPage.size());

    }

    @Test
    public void testBatchCouponsExcelOut() {
        Http.Response response = GET("/coupons/batchexport/" + batchCoupons.id + "/excel");
        assertIsOk(response);
        JPAExtPaginator<BatchCoupons> couponList = (JPAExtPaginator<BatchCoupons>) renderArgs("couponList");
        assertEquals(1, couponList.size());
    }


}
