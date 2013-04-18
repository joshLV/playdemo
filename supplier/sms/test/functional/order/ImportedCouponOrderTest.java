package functional.order;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.ImportedCoupon;
import models.sales.ImportedCouponStatus;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 导入券订单测试.
 * User: tanglq
 * Date: 13-4-17
 * Time: 下午5:23
 */
public class ImportedCouponOrderTest extends FunctionalTest {

    SupplierUser supplierUser;
    ResalerProduct product;
    Goods goods;
    ImportedCoupon importedCoupon;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        supplierUser = FactoryBoy.create(SupplierUser.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.couponType = GoodsCouponType.IMPORT;
            }
        });
        importedCoupon = FactoryBoy.create(ImportedCoupon.class);
        FactoryBoy.create(ImportedCoupon.class);
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.JD;
            }
        });

        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.JD_LOGIN_NAME;
            }
        });
        //创建可欠款账户
        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        ResalerFactory.getYibaiquanResaler(); //必须存在一百券
    }

    @Test
    public void test通过京东生成导入券订单() {
        Template template = TemplateLoader.load("test/data/jd.SendOrderRequest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("venderTeamId", product.goodsLinkId);
        String requestBody = template.render(params);
        long orderCount = Order.count();
        long couponCount = ECoupon.count();

        Http.Response response = POST("/api/v1/jd/gb/send-order", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);

        assertEquals(orderCount + 1, Order.count());
        assertEquals(couponCount + 2, ECoupon.count());


        OuterOrder outerOrder = OuterOrder.getOuterOrder("2323", OuterOrderPartner.JD);
        assertNotNull(outerOrder);

        assertNotNull(outerOrder.ybqOrder);
        assertEquals(OrderStatus.PAID, outerOrder.ybqOrder.status);

        ECoupon coupon1 = ECoupon.find("byOrderAndPartnerAndPartnerCouponId", outerOrder.ybqOrder, ECouponPartner.JD,
                "123").first();
        assertEquals(importedCoupon.coupon, coupon1.eCouponSn);
        assertEquals(importedCoupon.password, coupon1.eCouponPassword);
        assertEquals(ECouponStatus.CONSUMED, coupon1.status);
        assertEquals(supplierUser.id, coupon1.supplierUser.id);
        importedCoupon.refresh();
        assertEquals(ImportedCouponStatus.USED, importedCoupon.status);
    }

}
