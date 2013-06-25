package unit.jobs.order;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import jobs.goods.ScannerResalerProductStatusJob;
import models.jobs.JobWithHistory;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.ws.MockWebServiceClient;

import java.util.regex.Pattern;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午4:08
 */
public class ScannerResalerProductStatusTest extends UnitTest {
    ResalerProduct product;
    Resaler resaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        JobWithHistory.cleanLastBeginRunAtForTest();
        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.WUBA_LOGIN_NAME;
                target.onSaleKey = "class=\"buy_btn b_buy\"";
                target.offSaleKey = "class=\"buy_btn b_end\"";
            }
        });
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.status = ResalerProductStatus.UPLOADED;
                target.partner = OuterOrderPartner.WB;
                target.url = "http://t.58.com/sh/65460328362500006";
            }
        });
        MockWebServiceClient.clear();
    }

    @Test
    public void test_Job() {
        product.url = "http://tuan.jd.com/team-10515352.html";
        product.save();

        String succTxt = "<a href=\"/team/buy.php";
        Pattern onSalePattern = Pattern.compile("href=\"/team/buy.php");
        assertTrue(onSalePattern.matcher(succTxt).find());

        String endTxt = "<div class=\"buy_btn b_end\">";
        Pattern offSalePattern = Pattern.compile(resaler.offSaleKey);
        assertTrue(offSalePattern.matcher(endTxt).find());

        MockWebServiceClient.clear();
    }

    @Test
    public void test_Job_from_onsale_to_onSale() throws Exception {
        product.status = ResalerProductStatus.ONSALE;
        product.save();
        assertEquals(ResalerProductStatus.ONSALE, product.status);

        String resultXml = "<div class=\"buy_btn b_buy\"></div>";
        MockWebServiceClient.addMockHttpRequest(200, resultXml);
        ScannerResalerProductStatusJob job = new ScannerResalerProductStatusJob();
        job.doJob();
        product.refresh();
        assertEquals(ResalerProductStatus.ONSALE, product.status);
    }

    @Test
    public void test_Job_from_null_to_onSale() throws Exception {
        String resultXml = "<div class=\"buy_btn b_buy\"></div>";
        MockWebServiceClient.addMockHttpRequest(200, resultXml);
        ScannerResalerProductStatusJob job = new ScannerResalerProductStatusJob();
        job.doJob();
        product.refresh();
        assertEquals(ResalerProductStatus.ONSALE, product.status);
    }

    @Test
    public void test_Job_from_null_to_offSale() throws Exception {
        String resultXml = "<div class=\"buy_btn b_end\"></div>";
        MockWebServiceClient.addMockHttpRequest(200, resultXml);

        ScannerResalerProductStatusJob job = new ScannerResalerProductStatusJob();
        job.doJob();
        product.refresh();
        assertEquals(ResalerProductStatus.OFFSALE, product.status);
    }


    @Test
    public void test_Job_from_offsale_to_offSale() throws Exception {
        product.status = ResalerProductStatus.OFFSALE;
        product.save();
        String resultXml = "<div class=\"buy_btn b_end\"></div>";
        MockWebServiceClient.addMockHttpRequest(200, resultXml);
        assertEquals(ResalerProductStatus.OFFSALE, product.status);

        ScannerResalerProductStatusJob job = new ScannerResalerProductStatusJob();
        job.doJob();
        assertEquals(ResalerProductStatus.OFFSALE, product.status);
    }
}
