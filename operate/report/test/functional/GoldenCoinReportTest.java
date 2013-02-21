package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import models.sales.CheckinRelations;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 下午3:58
 */
public class GoldenCoinReportTest extends FunctionalTest {
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

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testGoldenIndex_测试金币明细() {
        final User user = FactoryBoy.lastOrCreate(User.class);
        FactoryBoy.batchCreate(20, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user;
                        target.remarks = "签到20天";
                        target.number = 5L;
                        target.createdAt = new Date();
                    }
                });

        FactoryBoy.create(UserGoldenCoin.class, "duihuan");
        FactoryBoy.create(UserGoldenCoin.class, "jl");
        Http.Response response = GET("/reports/golden-coins");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        JPAExtPaginator<UserGoldenCoin> reportPage = (JPAExtPaginator<UserGoldenCoin>) renderArgs("reportPage");
        assertNotNull(reportPage);
        assertEquals(22, reportPage.size());
        CheckinRelations summary = (CheckinRelations) renderArgs("summary");
        assertEquals(-1000, summary.number.intValue());
        assertEquals(200, summary.unUseNumber.intValue());

    }

    @Test
    public void testGoldenIndex_测试签到报表() {
        final User user = FactoryBoy.lastOrCreate(User.class);
        final Goods goods = FactoryBoy.create(Goods.class);

        FactoryBoy.batchCreate(4, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.goods = goods;
                        target.user = user;
                        target.remarks = "签到4天";
                        target.number = 5L;
                        target.createdAt = new Date();
                    }
                });
        final User user3 = FactoryBoy.create(User.class);
        FactoryBoy.batchCreate(10, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user3;
                        target.goods = goods;
                        target.remarks = "签到10天";
                        target.number = 5L;
                        target.createdAt = new Date();
                    }
                });
        FactoryBoy.create(UserGoldenCoin.class);
        FactoryBoy.create(UserGoldenCoin.class, "duihuan");
        FactoryBoy.create(UserGoldenCoin.class, "jl");
        Http.Response response = GET("/reports/checkin");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<CheckinRelations> reportPage = (ValuePaginator<CheckinRelations>) renderArgs("reportPage");
        assertNotNull(reportPage);
        assertEquals(4, reportPage.size());
        Long checkinNumber = (Long) renderArgs("summary");
        assertEquals(16, checkinNumber.intValue());


    }
}
