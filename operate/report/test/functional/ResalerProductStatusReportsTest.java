package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import factory.callback.BuildCallback;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午5:42
 */
public class ResalerProductStatusReportsTest extends FunctionalTest {
    ResalerProduct product;

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        product = FactoryBoy.create(ResalerProduct.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
    @Test
    public void testIndex_测试商品上下架_noCondition() {
        Http.Response response = GET("/reports/resaler-product");
        assertIsOk(response);
        List<ResalerProduct> resalerProductList = (List) renderArgs("reportPage");
        assertNotNull(resalerProductList);
        assertEquals(1, resalerProductList.size());
    }

    @Test
    public void testIndex_测试商品上下架_haveCondition() {

        final Goods goods = FactoryBoy.create(Goods.class);
        Resaler wubaResaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.WUBA_LOGIN_NAME;
            }
        });

        ResalerProduct wubaProduct = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                //To change body of implemented methods use File | Settings | File Templates.
                target.partner = OuterOrderPartner.WB;
                target.goods = goods;
                target.url = "http://yibaiquan.com/p/2";
            }
        });

        Resaler jdResaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.JD_LOGIN_NAME;
            }
        });
        ResalerProduct jingdongProduct = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.JD;
            }
        });

        Http.Response response = GET("/reports/resaler-product");
        assertIsOk(response);
        List<ResalerProduct> resalerProductList = (List) renderArgs("reportPage");
        assertNotNull(resalerProductList);
        assertEquals(2, resalerProductList.size());
    }

}
