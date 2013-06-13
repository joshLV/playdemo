package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountStatus;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import operate.rbac.RbacLoader;
import org.junit.*;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-12
 * Time: 上午10:05
 */
public class ImportPartnerOrdersTest extends FunctionalTest {
    ResalerProduct resalerProduct;
    Account account;
    Resaler resaler;
    Goods goods;
    OuterOrder outerOrder;
             Order order;
    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp"); //解决测试时上传失败的问题
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }


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

        ResalerFactory.getYibaiquanResaler(); //必须存在一百券

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.cumulativeStocks = 10000l;
            }
        });
        resaler = FactoryBoy.create(Resaler.class);
        resalerProduct = FactoryBoy.create(ResalerProduct.class);

        account = AccountUtil.getAccount(resaler.id, AccountType.RESALER);
        account.creditable = AccountCreditable.YES;
        account.status = AccountStatus.NORMAL;
        account.save();

    }

    @Test
    public void testImpOrder_JD_测试重复订单不会重复导入() {
        outerOrder = FactoryBoy.create(OuterOrder.class);
        outerOrder.orderId = "453172893";
        outerOrder.partner = OuterOrderPartner.JD;
        outerOrder.save();

        assertEquals(1, OuterOrder.count());
        resaler.loginName = Resaler.JD_LOGIN_NAME;
        resaler.save();

        resalerProduct.partnerProductId = "10494286";
        resalerProduct.partner = OuterOrderPartner.JD;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/JD_Orders.xlsx");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.JD.toString());
        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(1, OuterOrder.count());

    }

    @Test
    public void testImpOrder_JD_Success7AndUnbindGoods5() {
        resaler.loginName = Resaler.JD_LOGIN_NAME;
        resaler.save();

        resalerProduct.partnerProductId = "10528592";
        resalerProduct.partner = OuterOrderPartner.JD;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/JD_Orders.xlsx");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.JD.toString());
        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(7, OuterOrder.count());
        assertEquals(7, Order.count());
        //未映射商品
        Set<String> unBindGoods = (Set<String>) renderArgs("unBindGoodsSet");
        System.out.println("unBindGoods = " + unBindGoods);
        assertEquals(5, unBindGoods.size());

    }

    @Test
    public void testImpOrder_TB() {
        resaler.loginName = Resaler.TAOBAO_LOGIN_NAME;
        resaler.save();

        resalerProduct.partnerProductId = "23020076246";
        resalerProduct.partner = OuterOrderPartner.TB;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/TB_Orders.xlsx");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.TB.toString());
        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(1, OuterOrder.count());
        assertEquals(1, Order.count());

    }

    @Test
    public void testImpOrder_YHD() {
        resaler.loginName = Resaler.YHD_LOGIN_NAME;
        resaler.save();

        resalerProduct.partnerProductId = "0057930822";
        resalerProduct.partner = OuterOrderPartner.YHD;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/YHD_Orders.xlsx");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.YHD.toString());

        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(7, OuterOrder.count());
        assertEquals(7, Order.count());

    }

    @Test
    @Ignore
    public void testImpOrder_WB() {
        resaler.loginName = Resaler.WUBA_LOGIN_NAME;
        resaler.save();
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.cumulativeStocks = 10000l;
                target.salePrice = new BigDecimal("158");
            }
        });
        resalerProduct.partnerProductId = "DQ冰淇淋缤纷卡 200元DQ缤纷卡";
        resalerProduct.partner = OuterOrderPartner.WB;
        resalerProduct.goods = goods;
        resalerProduct.save();
        final Goods goods1 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.cumulativeStocks = 10000l;
                target.salePrice = new BigDecimal("158");
            }
        });
        ResalerProduct resalerProduct1 = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partnerProductId = "DQ冰淇淋缤纷卡 300元DQ缤纷卡";
                target.partner = OuterOrderPartner.WB;
                target.goods = goods1;

            }
        });
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/WB_Orders.xls");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.WB.toString());

        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(6, OuterOrder.count());
        assertEquals(6, Order.count());
        List<OrderItems> orderItemsList = OrderItems.findAll();
        assertEquals(7, orderItemsList.size());
        assertEquals(6, OrderShippingInfo.count());

    }

    @Test
    @Ignore
    public void testImpOrder_WB_unBind() {
        resaler.loginName = Resaler.WUBA_LOGIN_NAME;
        resaler.save();
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.cumulativeStocks = 10000l;
                target.salePrice = new BigDecimal("158");
            }
        });
        resalerProduct.partnerProductId = "DQ冰淇淋缤纷卡 200元DQ缤纷卡";
        resalerProduct.partner = OuterOrderPartner.WB;
        resalerProduct.goods = goods;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/WB_Orders.xls");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.WB.toString());

        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(3, OuterOrder.count());
        assertEquals(6, Order.count());
        //未映射商品
        Set<String> unBindGoods = (Set<String>) renderArgs("unBindGoodsList");
        assertEquals(3, unBindGoods.size());

        List<String> importSuccessOrderList = (List<String>) renderArgs("importSuccessOrderList");
        assertEquals(6, importSuccessOrderList.size());

    }

    @Test
    @Ignore
    public void testImpOrder_WB_DiffPrice() {
        resaler.loginName = Resaler.WUBA_LOGIN_NAME;
        resaler.save();
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.cumulativeStocks = 10000l;
                target.salePrice = new BigDecimal("168");
            }
        });
        resalerProduct.partnerProductId = "DQ冰淇淋缤纷卡 200元DQ缤纷卡";
        resalerProduct.partner = OuterOrderPartner.WB;
        resalerProduct.goods = goods;
        resalerProduct.save();
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/data/partnerOrder/WB_Orders.xls");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("orderFile", vfImage.getRealFile());
        Map<String, String> params = new HashMap<>();
        params.put("partner", OuterOrderPartner.WB.toString());

        Http.Response response = POST(Router.reverse("ImportPartnerOrders.upload").url, params, fileParams);
        assertIsOk(response);
        assertContentType("text/html", response);

        assertEquals(3, OuterOrder.count());
        assertEquals(6, Order.count());

        List<String> diffOrderPriceList = (List<String>) renderArgs("diffOrderPriceList");
        assertEquals(6, diffOrderPriceList.size());

    }
}
