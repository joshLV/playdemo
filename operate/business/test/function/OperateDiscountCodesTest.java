package function;

import java.math.BigDecimal;
import models.admin.OperateUser;
import models.order.DiscountCode;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

public class OperateDiscountCodesTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();

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
        ModelPaginator discountCodePage = (ModelPaginator)renderArgs("discountCodePage");
        assertEquals(10, discountCodePage.getRowCount());
    }    
}
