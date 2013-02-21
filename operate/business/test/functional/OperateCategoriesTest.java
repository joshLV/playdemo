package functional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Category;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 分类测试.
 * <p/>
 * User: sujie
 * Date: 1/9/13
 * Time: 5:47 PM
 */
public class OperateCategoriesTest extends FunctionalTest {
    Category parent;
    Category category;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

        parent = FactoryBoy.create(Category.class);
        category = FactoryBoy.create(Category.class);
        category.name="testcategory";
        category.parentCategory = parent;
        category.save();

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }

    @Test
    public void testShowSubs() {
        Http.Response response = GET("/category/sub/" + parent.id);
        assertIsOk(response);

        JsonArray categories = new JsonParser().parse(getContent(response)).getAsJsonArray();
        assertEquals(1, categories.size());
        for (JsonElement categoryJson : categories) {
            assertEquals("testcategory", categoryJson.getAsJsonObject().get("name").getAsString());
        }

    }

}
    