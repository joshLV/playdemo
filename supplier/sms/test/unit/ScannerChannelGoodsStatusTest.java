package unit;

import factory.FactoryBoy;
import models.sales.ChannelGoodsInfo;
import org.junit.Before;
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

//    public void test_Job() {
//
//    }

}
