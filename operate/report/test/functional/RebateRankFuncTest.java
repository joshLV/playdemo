package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
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
    User promoteUser;
    User inviteUser;
    PromoteRebate promoteRebate;

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);


        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        promoteUser = FactoryBoy.create(User.class);
        inviteUser = FactoryBoy.create(User.class, "loginName", new BuildCallback<User>() {
            @Override
            public void build(User target) {
            }
        });

        Order order = FactoryBoy.create(Order.class);
        promoteRebate = FactoryBoy.create(PromoteRebate.class);
        promoteRebate.rebateAmount = new BigDecimal(30);
        promoteRebate.promoteUser = promoteUser;
        promoteRebate.invitedUser = inviteUser;
        promoteRebate.promoteTimes=4l;
        promoteRebate.order = order;
        promoteRebate.save();
    }


    @Test
    public void testIndex() {
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
        assertEquals(10, summary.rebateAmount.intValue());
    }

}
