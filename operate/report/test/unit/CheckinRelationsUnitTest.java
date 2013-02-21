package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import models.sales.CheckinRelations;
import models.sales.GoldenCoinReportCondition;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 下午4:50
 */
public class CheckinRelationsUnitTest extends UnitTest {
    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);


        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
        final Goods goods = FactoryBoy.create(Goods.class);
        final User user = FactoryBoy.lastOrCreate(User.class);
        FactoryBoy.batchCreate(20, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.goods = goods;
                        target.user = user;
                        target.remarks = "签到20天";
                        target.number = 5L;
                        target.createdAt = new Date();
                    }
                });

        FactoryBoy.create(UserGoldenCoin.class);
        FactoryBoy.create(UserGoldenCoin.class, "duihuan");
    }

    @Test
    public void testGetCheckinList() {
        GoldenCoinReportCondition condition = new GoldenCoinReportCondition();
        List<CheckinRelations> relationsList = CheckinRelations.getCheckinList(condition);
        assertEquals(2, relationsList.size());

        Long checkinNumber = CheckinRelations.checkinSummary(relationsList);
        assertEquals(21, checkinNumber.intValue());
    }

    @Test
    public void testGetsummary() {
        JPAExtPaginator<UserGoldenCoin> reportPage = UserGoldenCoin.find(null, new UserCondition(), 1, 15);
        CheckinRelations relations = CheckinRelations.summary(reportPage);
        assertEquals(-1000, relations.number.intValue());
        assertEquals(105, relations.unUseNumber.intValue());
    }
}
