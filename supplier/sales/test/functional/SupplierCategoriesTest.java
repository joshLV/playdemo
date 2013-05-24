package functional;

import models.admin.SupplierUser;
import models.sales.Category;
import navigation.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

public class SupplierCategoriesTest extends FunctionalTest {

    Category category;

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();
        // f重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        category = FactoryBoy.create(Category.class);
        FactoryBoy.batchBuild(3, Category.class, new SequenceCallback<Category>() {
            @Override
            public void sequence(Category cat, int seq) {
                cat.parentCategory = category;
            }
        });
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
    
    @Test
    public void testSubcat() throws Exception {
        Response response = GET("/category/sub/" + category.id);
        assertIsOk(response);
    }
}
