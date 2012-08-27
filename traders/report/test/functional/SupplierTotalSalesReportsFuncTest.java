package functional;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-22
 * Time: 下午4:35
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class SupplierTotalSalesReportsFuncTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);


        // todo 登陆失败，需要配置permission
        final SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化数据
        FactoryBoy.batchCreate(10, ECoupon.class,new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon target, int seq) {
                target.isFreeze = 0;
                target.status = ECouponStatus.CONSUMED;
                target.eCouponSn = "1000"+seq;
                target.supplierUser = user;
                target.faceValue = new BigDecimal(100);
                target.originalPrice = new BigDecimal(80);
                target.salePrice = new BigDecimal(90);
                target.consumedAt = new Date();
            }
        });

    }

//    @Test
//    public void testTrends(){
//
//        Http.Response response = GET("/totalsales/trends?condition.type=1&condition.shopId=0&condition.beginAt=&condition.endAt=&condition.interval=");
//        assertStatus(302,response);
//        assertNotNull(renderArgs("totalSales"));
//        //assertNotNull(renderArgs("dateList"));
//        //assertNotNull(renderArgs("chartsMap"));
//        //assertNotNull(renderArgs("reportPage"));
//
//    }
}
