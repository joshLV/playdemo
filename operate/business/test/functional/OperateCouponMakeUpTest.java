package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.ws.MockWebServiceClient;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-17
 * Time: 下午2:23
 */
public class OperateCouponMakeUpTest extends FunctionalTest {
    OperateUser user;
    ECoupon eCoupon;
    Resaler resaler;

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化 电子券数据
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class);

        FactoryBoy.batchCreate(10, ECoupon.class, "Id",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.shop = shop;
                        target.goods = goods;
                        target.eCouponSn = "8888000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });
        resaler = FactoryBoy.create(Resaler.class);
        resaler.loginName = "taobao";
        resaler.save();

        eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.partner = ECouponPartner.TB;
        eCoupon.save();
        MockWebServiceClient.clear();
    }

    @Test
    public void testIndex_withoutParams() {
        Http.Response response = GET("/makeup");
        assertIsOk(response);
        assertContentMatch("请输入partner和coupon,多个coupon请用半角逗号分割", response);
    }

    @Test
    public void testIndex_ValidCoupon() {

        Http.Response response = GET("/makeup?partner=TB&coupon=11111");
        assertIsOk(response);
        assertContentMatch("没有找到", response);
    }

    @Test
    public void testIndex_Coupon() {
        Http.Response response = GET("/makeup?partner=TB&coupon=" + eCoupon.eCouponSn);
        assertIsOk(response);
        assertContentMatch("状态不是已消费", response);
    }

    @Test
    public void testIndex_noPartner() {
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.partner = ECouponPartner.DD;
        eCoupon.save();
        Http.Response response = GET("/makeup?partner=TB&coupon=" + eCoupon.eCouponSn);
        assertIsOk(response);
        assertContentMatch("在第三方消费失败", response);
    }

    @Test
    public void testIndex_Consumed_DDCoupon() {
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.partner = ECouponPartner.DD;
        eCoupon.save();
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><ver>1.0</ver><spid>1</spid><error_code>0</error_code>" +
                "<desc>success</desc><data><ddgid>100</ddgid><spgid>100</spgid>" +
                "<ddsn>1344555</ddsn></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);

        Http.Response response = GET("/makeup?partner=dangdang&coupon=" + eCoupon.eCouponSn);
        assertIsOk(response);

        assertContentMatch("输入：" + eCoupon.eCouponSn, response);
    }
}
