package functional.resale;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.ResalerProduct;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * User: yan
 * Date: 13-3-28
 * Time: 上午10:57
 */
public class SinaVouchersTest extends FunctionalTest {
    ResalerProduct product;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        product = FactoryBoy.create(ResalerProduct.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.brand = FactoryBoy.lastOrCreate(Brand.class);
            }
        });

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        MockWebServiceClient.clear();
    }

    @Test
    public void testShowUpload() {
        Map<String, Object> params = new HashMap<>();
        params.put("goodsId", goods.id);
        Http.Response response = GET(Router.reverse("controllers.resale.SinaVouchers.showUpload", params));
        assertIsOk(response);
        assertEquals(goods, renderArgs("goods"));
    }
}
