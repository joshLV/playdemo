package functional;

import controllers.modules.website.cas.Security;
import models.consumer.Address;
import models.consumer.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-27
 * Time: 下午6:03
 * To change this template use File | Settings | File Templates.
 */
public class UserAddressesFuncTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(User.class);

        //Fixtures.loadModels("fixture/user.yml", "fixture/userInfo.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/userInfo.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/brands.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/shops.yml");
        Fixtures.loadModels("fixture/goods.yml");
        Fixtures.loadModels("fixture/carts.yml");
        Fixtures.loadModels("fixture/addresses.yml");


        Long userId= (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(userId);

        //设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testCreate(){

        Map<String,String> params = new HashMap<>();
        params.put("address.name","testName");
        params.put("address.province","上海市");
        params.put("address.city","市辖区");
        params.put("address.district","黄浦区");
        params.put("address.address","宛平南路100号");
        params.put("address.postcode","200000");
        params.put("address.mobile","15888888888");

        Http.Response response = POST("/addresses",params);
        assertStatus(302,response);
        long id = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(id);
        int size = Address.findByOrder(user).size();
        assertEquals(3,size);

    }

    @Test
    public void testCreateAndDefault(){

        Map<String,String> params = new HashMap<>();
        params.put("address.name","testName");
        params.put("address.province","上海市");
        params.put("address.city","市辖区");
        params.put("address.district","黄浦区");
        params.put("address.address","宛平南路100号");
        params.put("address.postcode","200000");
        params.put("address.mobile","15888888888");
        params.put("address.isDefault","true");

        Http.Response response = POST("/addresses",params);
        assertStatus(302,response);
        long id = (Long) Fixtures.idCache.get("models.consumer.User-selenium");
        User user = User.findById(id);
        int size = Address.findByOrder(user).size();
        assertEquals(3,size);

    }

    //@Test  TODO
    public void testUpdate(){
        long id = (Long) Fixtures.idCache.get("models.consumer.Address-test1");
        String params = "address.name=testName" +
                "&address.province=上海市" +
                "&address.city=市辖区" +
                "&address.district=黄浦区" +
                "&address.address=宛平南路" +
                "&address.postcode=200000" +
                "&address.mobile=12345678900";

        Http.Response response = PUT("/addresses/"+id,"text/html",params);
        assertStatus(302,response);
    }

}
