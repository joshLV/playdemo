package unit.jobs.order;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import jobs.order.SendGoldenCoinsJob;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午5:13
 */
public class SendGoldenCoinsJobTest extends UnitTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testJob_测试签到10天() throws Exception {
        final User user = FactoryBoy.lastOrCreate(User.class);
        FactoryBoy.batchCreate(10, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user;
                        target.remarks = "签到10天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateUtil.lastDayOfMonth(new Date());
                    }
                });
        Long cnt = UserGoldenCoin.count();
        assertEquals(10, cnt.intValue());
        SendGoldenCoinsJob job = new SendGoldenCoinsJob();
        job.doJob();
        assertEquals(10, cnt.intValue());
    }

    @Test
    public void testJob_测试签到21天() throws Exception {
        final User user3 = FactoryBoy.create(User.class);
        FactoryBoy.batchCreate(21, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user3;
                        target.remarks = "签到21天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateHelper.afterDays(DateUtil.lastMonthOfFirstDay(), 1);
                    }
                });

        Long cnt = UserGoldenCoin.count();
        assertEquals(21, cnt.intValue());
        SendGoldenCoinsJob job = new SendGoldenCoinsJob();
        job.doJob();


        UserGoldenCoin goldenCoin = UserGoldenCoin.find("user=? order by createdAt desc", user3).first();
        assertEquals(22, UserGoldenCoin.count());
        assertEquals(100, goldenCoin.checkinNumber.intValue());
    }

    @Test
    public void testJob_测试签到20天() throws Exception {
        final User user3 = FactoryBoy.create(User.class);
        FactoryBoy.batchCreate(20, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user3;
                        target.remarks = "签到20天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateHelper.afterDays(DateUtil.lastMonthOfFirstDay(), 1);

                    }
                });
        Long cnt = UserGoldenCoin.count();
        assertEquals(20, cnt.intValue());
        SendGoldenCoinsJob job = new SendGoldenCoinsJob();
        job.doJob();

        UserGoldenCoin goldenCoin = UserGoldenCoin.find("user=? order by createdAt desc", user3).first();

        assertEquals(21, UserGoldenCoin.count());
        assertEquals(100, goldenCoin.checkinNumber.intValue());
    }

    @Test
    public void testJob_测试满勤31天() throws Exception {
        final User user2 = FactoryBoy.create(User.class);
        FactoryBoy.batchCreate(31, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user2;
                        target.remarks = "签到31天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateHelper.afterDays(DateUtil.lastMonthOfFirstDay(), 1);
                    }
                });
        Long cnt = UserGoldenCoin.count();
        assertEquals(31, cnt.intValue());
        SendGoldenCoinsJob job = new SendGoldenCoinsJob();
        job.doJob();

        UserGoldenCoin goldenCoin = UserGoldenCoin.find("user=? order by createdAt desc", user2).first();

        assertEquals(32, UserGoldenCoin.count());
        assertEquals(350, goldenCoin.checkinNumber.intValue());
    }

    @Test
    public void testJob_测试满勤30天() throws Exception {
        final User user1 = FactoryBoy.create(User.class);
        FactoryBoy.batchCreate(30, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.user = user1;
                        target.remarks = "签到30天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateHelper.afterDays(DateUtil.lastMonthOfFirstDay(), 1);
                    }
                });

        Long cnt = UserGoldenCoin.count();
        assertEquals(30, cnt.intValue());
        SendGoldenCoinsJob job = new SendGoldenCoinsJob();
        job.doJob();

        UserGoldenCoin goldenCoin = UserGoldenCoin.find("user=? order by createdAt desc", user1).first();

        assertEquals(31, UserGoldenCoin.count());
        assertEquals(350, goldenCoin.checkinNumber.intValue());

    }


}
