package unit;

import java.util.List;

import models.order.ECouponCompensation;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import util.DateHelper;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * 分销渠道补偿验证记录。
 * 
 * 有时没有把消费记录同步到分销商，则通过这个功能进行。
 * @author tanglq
 *
 */
public class ECouponCompensationTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(ECouponCompensation.class);
        FactoryBoy.create(ECouponCompensation.class, new BuildCallback<ECouponCompensation>() {
            @Override
            public void build(ECouponCompensation target) {
                target.compensatedAt = DateHelper.beforeDays(1);
                target.result = "SUCCESS";
            }
        });
    }
    
    @Test
    public void testFindECouponConfirmCompensations() throws Exception {
        assertEquals(2, ECouponCompensation.count());
        List<ECouponCompensation> list = ECouponCompensation.findTodoCompensations(ECouponCompensation.CONSUMED);
        assertEquals(1, list.size());
    }

    @Test
    public void testFindECouponNotExistsCompensations() throws Exception {
        List<ECouponCompensation> list = ECouponCompensation.findTodoCompensations("NONE");
        assertEquals(0, list.size());
    }
}
