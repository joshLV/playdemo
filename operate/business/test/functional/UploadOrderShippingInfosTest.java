package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.order.*;
import models.sales.Goods;
import models.sales.Sku;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.*;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-15
 * Time: 上午9:50
 */
public class UploadOrderShippingInfosTest extends FunctionalTest {
    OrderItems orderItems;
    OrderItems orderItems1;
    Sku sku;
    ExpressCompany expressCompany;
    Goods goods;
    OrderShippingInfo orderShippingInfo;
    OrderShippingInfo orderShippingInfo1;

    /**
     * 测试数据准备
     */
    @Before
    public void setup() {
        // 重新加载配置文件
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.create(ExpressCompany.class);
        sku = FactoryBoy.create(Sku.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.sku = sku;
                target.code = "1";
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class);
        orderShippingInfo = FactoryBoy.create(OrderShippingInfo.class);
        orderShippingInfo1 = FactoryBoy.create(OrderShippingInfo.class);
    }


    @After
    public void tearDown() throws Exception {
        Security.cleanLoginUserForTest();

    }

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp"); //解决测试时上传失败的问题
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("UploadOrderShippingInfos.index").url);
        assertIsOk(response);
        assertContentType("text/html", response);
        List<Supplier> supplierList = (List) renderArgs("supplierList");
        assertEquals(1, supplierList.size());
    }

    @Test
    public void testUpload() {
        orderItems.goods = goods;
        orderItems.order.orderNumber = "15558146";
        orderItems.order.save();
        orderItems.status = OrderStatus.PREPARED;
        orderItems.shippingInfo = orderShippingInfo;
        orderItems.save();

        Order order = FactoryBoy.create(Order.class);
        order.orderNumber = "18600152";
        order.save();

        orderItems1 = FactoryBoy.create(OrderItems.class);
        orderItems1.goods = goods;
        orderItems1.order = order;
        orderItems1.shippingInfo = orderShippingInfo1;
        orderItems1.status = OrderStatus.PREPARED;
        orderItems1.save();
        assertEquals(OrderStatus.PREPARED, orderItems.status);
        assertNull(orderItems.shippingInfo.expressCompany);
        assertEquals(OrderStatus.PREPARED, orderItems1.status);
        assertNull(orderItems1.shippingInfo.expressCompany);

        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/order_shipping.xlsx");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderShippingFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        Http.Response response = POST(Router.reverse("UploadOrderShippingInfos.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);
        orderItems.refresh();
        orderShippingInfo.refresh();
        assertEquals(OrderStatus.SENT, orderItems.status);
        assertEquals("test", orderShippingInfo.expressCompany.code);
        assertEquals("1234568", orderShippingInfo.expressNumber);
        orderItems1.refresh();
        orderShippingInfo1.refresh();
        assertEquals(OrderStatus.SENT, orderItems1.status);
        assertEquals("test", orderShippingInfo1.expressCompany.code);
        assertEquals("12345689", orderShippingInfo1.expressNumber);
    }

}
