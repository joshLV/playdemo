package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.operator.Operator;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import models.sales.Goods;
import models.sales.ResalerProductStatus;
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
public class ResalerProductsTest extends FunctionalTest {
    Resaler tbResaler;
    Resaler jdResaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        final OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        Operator operator=FactoryBoy.lastOrCreate(Operator.class);
        tbResaler = FactoryBoy.create(Resaler.class);
        tbResaler.partner = "TB";
        tbResaler.operator= operator;
        tbResaler.loginName = Resaler.TAOBAO_LOGIN_NAME;
        tbResaler.save();
        jdResaler = FactoryBoy.create(Resaler.class);
        jdResaler.partner = "JD";
        jdResaler.operator= operator;
        jdResaler.loginName = Resaler.JD_LOGIN_NAME;
        jdResaler.save();
        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.creatorId = user.id;
                target.lastModifierId = user.id;
                target.resaler = tbResaler;
                target.status = ResalerProductStatus.UPLOADED;
            }
        });
        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.creatorId = user.id;
                target.lastModifierId = user.id;
                target.partner = OuterOrderPartner.JD;
                target.resaler = jdResaler;
                target.status = ResalerProductStatus.UPLOADED;
            }
        });

    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("controllers.ResalerProducts.index"));
        assertIsOk(response);
        JPAExtPaginator<Goods> goodsPage = (JPAExtPaginator<models.sales.Goods>) renderArgs("goodsPage");
        assertNotNull(goodsPage);
        assertEquals(1, goodsPage.size());
        Goods goods = goodsPage.get(0);

        Map<String, List<ResalerProduct>> partnerProducts = (Map<String, List<ResalerProduct>>) renderArgs("partnerProducts");
        assertNotNull(partnerProducts);
        assertNotNull(partnerProducts.get(goods.id + "-" + jdResaler.id.toString()));
        assertEquals(1, partnerProducts.get(goods.id + "-" + jdResaler.id.toString()).size());
        assertNotNull(partnerProducts.get(goods.id + "-" + tbResaler.id.toString()));
        assertEquals(1, partnerProducts.get(goods.id + "-" + tbResaler.id.toString()).size());
    }

}
