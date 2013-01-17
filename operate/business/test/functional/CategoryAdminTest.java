package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateUser;
import models.sales.Category;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-10-26
 * Time: 下午5:39
 */
public class CategoryAdminTest extends FunctionalTest {
    Category category;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        category = FactoryBoy.create(Category.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/category");
        assertStatus(200, response);
        List<Category> categoryList = (List<Category>) renderArgs("categoryList");
        assertEquals(1, categoryList.size());
        assertEquals(category.name, categoryList.get(0).name);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/category/" + category.id + "/edit");
        assertStatus(200, response);
        assertEquals(category.name, renderArgs("category.name"));
    }

    @Test
    public void testUpdate() {
        String name = "美食";
        String params = "category.name=" + name;
        Http.Response response = PUT("/category/" + category.id, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        assertEquals(name, renderArgs("category.name"));
    }

    @Test
    public void testAdd() {
        Http.Response response;
        if (category.parentCategory == null) {
            response = GET("/category/new?parentId=" + null);
        } else {
            response = GET("/category/new?parentId=" + category.parentCategory.id);
        }
        assertStatus(200, response);
        if (category.parentCategory == null) {
            assertEquals(null, renderArgs("parentId"));
        } else {
            assertEquals(category.parentCategory.id, renderArgs("parentId"));
        }
    }

    @Test
    public void testCreate() {
        assertEquals(1, Category.count());
        Map<String, String> itemParams = new HashMap<>();
        String name = "美食";
        itemParams.put("category.name", name);
        itemParams.put("category.displayOrder", category.displayOrder.toString());
        itemParams.put("category.isInWWWLeft", category.isInWWWLeft.toString());
        itemParams.put("category.isInWWWFloor", category.isInWWWFloor.toString());
        itemParams.put("category.display", category.display.toString());
        Http.Response response = POST("/category", itemParams);
        assertStatus(302, response);
        assertEquals(2, Category.count());
    }

    @Test
    public void testDelete() {
        List<Category> categoryList = Category.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        assertEquals(1, categoryList.size());
        Http.Response response = DELETE("/category/" + category.id);
        assertStatus(302, response);
        categoryList = Category.find("deleted=?", DeletedStatus.UN_DELETED).fetch();
        assertEquals(0, categoryList.size());
    }


}
