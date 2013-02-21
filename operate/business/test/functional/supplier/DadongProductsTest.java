package functional.supplier;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import util.mq.MockMQ;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 大东票务接口测试.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午4:44
 */
public class DadongProductsTest extends FunctionalTest {

    Supplier dadong;
    OperateUser operateUser;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        MockWebServiceClient.clear();
        dadong = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier target) {
                target.domainName = "dadong";
            }
        });

        FactoryBoy.create(Category.class, new BuildCallback<Category>() {
            @Override
            public void build(Category target) {
                target.name = "旅游票务";
            }
        });
        FactoryBoy.create(Brand.class);
        operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        Security.cleanLoginUserForTest();
        MockMQ.clear();
    }

    @Test
    public void testSync() throws Exception {
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/GetProductsPage1.xml");
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/GetProductsEndPage.xml");

        Map<String, String> params = new HashMap<>();
        Http.Response response = POST(Router.reverse("supplier.DadongProducts.sync").url, params);
        assertIsOk(response);

        assertEquals(7, Goods.count());
        assertEquals(7, Shop.count());
        int newCount = (Integer) renderArgs("newCount");
        assertEquals(7, newCount);
    }
}
