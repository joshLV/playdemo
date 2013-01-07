package unit;

import factory.FactoryBoy;
import models.job.ScannerChannelGoodsStatusJob;
import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.ChannelGoodsInfoStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午4:08
 */
public class ScannerChannelGoodsStatusTest extends UnitTest {
    ChannelGoodsInfo channelGoodsInfo;
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        resaler.loginName = "wuba";
        resaler.onSaleKey = "class=\"buy_btn b_buy\"";
        resaler.offSaleKey = "class=\"buy_btn b_end\"";
        resaler.save();
        channelGoodsInfo = FactoryBoy.create(ChannelGoodsInfo.class);
        channelGoodsInfo.resaler = resaler;
        channelGoodsInfo.url = "http://t.58.com/sh/65460328362500006";
        channelGoodsInfo.save();

    }
//    @Test
//    public void test_Job() {
//        String succTxt = "<div class=\"buy_btn b_buy\">";
//        Pattern onSalePattern = Pattern.compile(resaler.onSaleKey);
//        assertTrue(onSalePattern.matcher(succTxt).find());
//
//        String endTxt = "<div class=\"buy_btn b_end\">";
//        Pattern offSalePattern = Pattern.compile(resaler.offSaleKey);
//        assertTrue(offSalePattern.matcher(endTxt).find());
//    }
    @Test
    public void test_Job_from_onsale_to_onSale() {
        channelGoodsInfo.status = ChannelGoodsInfoStatus.ONSALE;
        channelGoodsInfo.save();
        assertEquals(ChannelGoodsInfoStatus.ONSALE, channelGoodsInfo.status);

        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
        assertEquals(ChannelGoodsInfoStatus.ONSALE, channelGoodsInfo.status);
    }

    @Test
    public void test_Job_from_null_to_onSale() {
        assertNull(channelGoodsInfo.status);
        assertNull(channelGoodsInfo.onSaleAt);
        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
        assertEquals(ChannelGoodsInfoStatus.ONSALE, channelGoodsInfo.status);
        assertNotNull(channelGoodsInfo.onSaleAt);
    }

    @Test
    public void test_Job_from_null_to_offSale() {
        channelGoodsInfo.url = "http://t.58.com/sh/65589866425859008";
        channelGoodsInfo.save();
        assertNull(channelGoodsInfo.status);
        assertNull(channelGoodsInfo.offSaleAt);
        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
        assertEquals(ChannelGoodsInfoStatus.OFFSALE, channelGoodsInfo.status);
        assertNotNull(channelGoodsInfo.offSaleAt);
    }



    @Test
    public void test_Job_from_offsale_to_offSale() {
        channelGoodsInfo.status = ChannelGoodsInfoStatus.OFFSALE;
        channelGoodsInfo.offSaleAt = new Date();
        channelGoodsInfo.url = "http://t.58.com/sh/65589866425859008";
        channelGoodsInfo.save();
        assertEquals(ChannelGoodsInfoStatus.OFFSALE, channelGoodsInfo.status);
        assertNotNull(channelGoodsInfo.offSaleAt);

        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
        assertEquals(ChannelGoodsInfoStatus.OFFSALE, channelGoodsInfo.status);
        assertNotNull(channelGoodsInfo.offSaleAt);
    }

    @Test
    public void test_Job_noPage() {
        channelGoodsInfo.url = "http://t.58.com/sh/655898664258590081";
        channelGoodsInfo.save();
        assertNull(channelGoodsInfo.status);
        assertNull(channelGoodsInfo.offSaleAt);

        ScannerChannelGoodsStatusJob job = new ScannerChannelGoodsStatusJob();
        job.doJob();
        assertEquals(ChannelGoodsInfoStatus.PAGE_NOT_EXISTED, channelGoodsInfo.status);
        assertNull(channelGoodsInfo.offSaleAt);
    }
}