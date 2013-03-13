package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountStatus;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import operate.rbac.RbacLoader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
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
        Set<String> unBindGoods = (Set<String>) renderArgs("unBindGoodsList");
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
}
