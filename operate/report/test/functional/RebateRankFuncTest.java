package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.PromoteRebate;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-25
 * Time: 下午4:15
 */
public class RebateRankFuncTest extends FunctionalTest {
    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


    @Test
    public void testIndex() {
        PromoteRebate promoteRebate = FactoryBoy.create(PromoteRebate.class);
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.status = ECouponStatus.CONSUMED;
        coupon.consumedAt = new Date();
        coupon.promoterRebateValue = BigDecimal.TEN;
        coupon.order = promoteRebate.order;
        coupon.save();
        Http.Response response = GET("/reports/rank");
        assertStatus(200, response);
        assertContentMatch("返利排名报表", response);
        assertNotNull(renderArgs("rankList"));
        ValuePaginator<PromoteRebate> reportPage = (ValuePaginator<PromoteRebate>) renderArgs("rankList");
        assertEquals(1, reportPage.getRowCount());
        PromoteRebate summary = (PromoteRebate) (renderArgs("summary"));
        assertEquals(1, summary.promoteTimes);
        System.out.println(summary.rebateAmount);
        assertEquals(10, summary.rebateAmount.intValue());
    }

}
