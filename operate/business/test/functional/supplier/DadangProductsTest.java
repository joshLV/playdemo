package functional.supplier;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

/**
 * 大东票务接口测试.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午4:44
 */
public class DadangProductsTest extends FunctionalTest {

    Supplier dadong;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        dadong = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier target) {
                target.domainName = "dadong";
            }
        });
    }

    @Test
    public void testSync() throws Exception {
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/GetProductsEndPage.xml");
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/GetProductsPage2.xml");
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/GetProductsPage1.xml");
    }
}