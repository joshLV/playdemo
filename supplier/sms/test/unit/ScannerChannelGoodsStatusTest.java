package unit;

import factory.FactoryBoy;
import models.job.ScannerChannelGoodsStatusJob;
import models.sales.ChannelGoodsInfo;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午4:08
 */
public class ScannerChannelGoodsStatusTest extends UnitTest {
    ChannelGoodsInfo channelGoodsInfo;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void test_Job() {
        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
    }

}
