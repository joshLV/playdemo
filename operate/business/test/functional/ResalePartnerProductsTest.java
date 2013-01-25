package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-11
 */
public class ResalePartnerProductsTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        final OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.creatorId = user.id;
                target.lastModifierId = user.id;
            }
        });
        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.creatorId = user.id;
                target.lastModifierId = user.id;
                target.partner = OuterOrderPartner.JD;
            }
        });

    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("controllers.ResalePartnerProducts.index"));
        assertIsOk(response);
        JPAExtPaginator<Goods> goodsPage = (JPAExtPaginator<models.sales.Goods>)renderArgs("goodsPage");
        assertNotNull(goodsPage);
        assertEquals(1, goodsPage.size());
        Goods goods = goodsPage.get(0);

        Map<String, List<ResalerProduct> > partnerProducts = (Map<String, List<ResalerProduct> > ) renderArgs("partnerProducts");
        assertNotNull(partnerProducts);
        assertNotNull(partnerProducts.get(goods.id + OuterOrderPartner.JD.toString()));
        assertEquals(1, partnerProducts.get(goods.id + OuterOrderPartner.JD.toString()).size());
        assertNotNull(partnerProducts.get(goods.id + OuterOrderPartner.TB.toString()));
        assertEquals(1, partnerProducts.get(goods.id + OuterOrderPartner.TB.toString()).size());
    }

}
