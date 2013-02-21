package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.order.DiscountCode;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Error;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperateDiscountCodesTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.batchCreate(10, DiscountCode.class,
                new SequenceCallback<DiscountCode>() {
                    @Override
                    public void sequence(DiscountCode target, int seq) {
                        target.discountAmount = BigDecimal.TEN;
                        target.discountSn = "TEST" + seq;
                    }
                });

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void 测试正常访问列表页() {
        Response response = GET("/discountcodes");

        assertIsOk(response);
        assertContentType("text/html", response);
        assertNull(renderArgs("discountSN"));
        assertNotNull(renderArgs("discountCodePage"));
    }

    @Test
    public void 测试查询访问列表页() {
        FactoryBoy.batchCreate(10, DiscountCode.class,
                new SequenceCallback<DiscountCode>() {
                    @Override
                    public void sequence(DiscountCode target, int seq) {
                        target.discountAmount = BigDecimal.ONE;
                        target.discountSn = "abc" + seq;
                    }
                });

        Response response = GET("/discountcodes?sn=abc");

        assertIsOk(response);
        assertContentType("text/html", response);

        assertNotNull(renderArgs("discountSN"));
        assertEquals("abc", renderArgs("discountSN").toString());
        assertNotNull(renderArgs("discountCodePage"));
        ModelPaginator discountCodePage = (ModelPaginator) renderArgs("discountCodePage");
        assertEquals(10, discountCodePage.getRowCount());
    }

    @Test
    public void 测试打开添加页面() {

        // Http.Response response = GET("/discountcodes/add");
        // assertIsOk(response);
        // assertContentMatch("添加折扣券",response);

    }

    @Test
    public void 测试创建新折扣券() {
        Goods goods = FactoryBoy.create(Goods.class);

        Map<String, String> params = new HashMap<>();
        params.put("discountCode.title", "测试用折扣券");
        params.put("discountCode.discountSn", "QQ");
        params.put("discountCode.description", "描述");
        params.put("discountCode.goods.id", goods.id.toString());
        params.put("discountCode.discountAmount", "1");
        params.put("discountCode.beginAt", "2012-08-01 17:08:59");
        params.put("discountCode.endAt", "2012-09-01 17:08:59");

        Http.Response response = POST("/discountcodes", params);
        assertStatus(302, response);
        // 查询折扣券是否已经被保存
        List<DiscountCode> discountCodes = DiscountCode.findAll();
        boolean b = false;
        for (DiscountCode discountCode : discountCodes) {
            if (discountCode.discountSn.equals("QQ")) {
                b = true;
            }
        }
        assertTrue(b);

    }

    @Test
    public void 测试打开指定折扣券修改页面() {
        DiscountCode discountDode = FactoryBoy.create(DiscountCode.class);

        Http.Response response = GET("/discountcodes/" + discountDode.id.toString() + "/edit");
        assertIsOk(response);
        assertContentMatch("修改折扣券", response);
    }

    @Test
    public void 测试删除制定折扣券() {
        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);

        Http.Response response = DELETE("/discountcodes/" + discountCode.id.toString());
        assertStatus(302, response);
        discountCode.refresh();
        assertEquals(DeletedStatus.DELETED, discountCode.deleted);

    }

    @Test
    public void 测试更新指定折扣券() {

        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        Goods goods = FactoryBoy.create(Goods.class);

        String params = "?discountCode.title=测试用折扣券&discountCode.discountSn=QQ" +
                "&discountCode.goods.id=" + goods.id.toString() +
                "&discountCode.discountAmount=1&discountCode.beginAt=2012-08-01 17:08:59" +
                "&discountCode.endAt=2012-09-01 17:08:59";

        Http.Response response = PUT("/discountcodes/" + discountCode.id.toString(), "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        discountCode = DiscountCode.findById(discountCode.id);
        discountCode.refresh();
        assertEquals("QQ", discountCode.discountSn);

    }


    @Test
    public void testCreateInvalid() {
        Goods goods = FactoryBoy.create(Goods.class);

        Map<String, String> params = new HashMap<>();
        params.put("discountCode.title", "测试用折扣券");
        params.put("discountCode.discountSn", "QQ");
        params.put("discountCode.description", "描述");
        params.put("discountCode.goods.id", goods.id.toString());
        params.put("discountCode.discountAmount", "1");
        params.put("discountCode.endAt", "2012-09-01 17:08:59");

        Http.Response response = POST("/discountcodes", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("discountCode.beginAt", errors.get(0).getKey());
        assertStatus(200, response);


    }

    @Test
    public void testUpdateInvalid() {

        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        Goods goods = FactoryBoy.create(Goods.class);

        String params = "?discountCode.title=测试用折扣券&discountCode.discountSn=QQ" +
                "&discountCode.goods.id=" + goods.id.toString() +
                "&discountCode.discountAmount=1&discountCode.beginAt=2012-08-01 17:08:59";
        Http.Response response = PUT("/discountcodes/" + discountCode.id.toString(), "application/x-www-form-urlencoded", params);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("discountCode.endAt", errors.get(0).getKey());
        assertStatus(200, response);
    }

}
