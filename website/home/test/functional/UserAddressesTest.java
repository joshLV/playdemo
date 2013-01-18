package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Error;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户地址功能测试类.
 * <p/>
 * User: Juno
 * Date: 12-7-27
 * Time: 下午6:03
 */
public class UserAddressesTest extends FunctionalTest {

    User user;
    UserInfo userInfo;
    Address address;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        userInfo = FactoryBoy.create(UserInfo.class);
        address = FactoryBoy.create(Address.class);

        //设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("UserAddresses.index").url);
        assertIsOk(response);

        List<Address> addressList = (List<Address>) renderArgs("addressList");
        BreadcrumbList breadcrumbs = (BreadcrumbList) renderArgs("breadcrumbs");

        assertEquals("收货地址", breadcrumbs.get(0).desc);
        assertEquals("/addresses", breadcrumbs.get(0).url);
        assertEquals(1, addressList.size());
    }

    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        params.put("address.name", "testName");
        params.put("address.province", "上海市");
        params.put("address.city", "市辖区");
        params.put("address.district", "黄浦区");
        params.put("address.address", "宛平南路100号");
        params.put("address.postcode", "200000");
        params.put("address.mobile", "15888888888");

        Http.Response response = POST("/addresses", params);
        assertStatus(302, response);
        int size = Address.findByOrder(user).size();
        assertEquals(2, size);
    }

    @Test
    public void testCreate_InvalidPhone() {
        Map<String, String> params = new HashMap<>();
        params.put("address.name", "testName");
        params.put("address.province", "上海市");
        params.put("address.city", "市辖区");
        params.put("address.district", "黄浦区");
        params.put("address.address", "宛平南路100号");
        params.put("address.postcode", "200000");

        Http.Response response = POST("/addresses", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("address.mobile", errors.get(0).getKey());
        assertIsOk(response);
    }

    @Test
    public void testCreate_InvalidAddress() {
        Map<String, String> params = new HashMap<>();
        params.put("address.name", "testName");
        params.put("address.province", "上海市");
        params.put("address.city", "市辖区");
        params.put("address.district", "黄浦区");
        params.put("address.postcode", "200000");
        params.put("address.mobile", "15888888888");

        Http.Response response = POST("/addresses", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("address.address", errors.get(0).getKey());
        assertIsOk(response);
    }


    @Test
    public void testEdit() {
        Http.Response response = GET("/addresses/" + address.id + "/edit");
        assertIsOk(response);

        List<Address> addressList = (List<Address>) renderArgs("addressList");
        Address renderAddress = (Address) renderArgs("address");

        assertEquals(1, addressList.size());
        assertEquals(address.id, renderAddress.id);
        assertEquals(address.address, renderAddress.address);
    }


    @Test
    public void testCreateAndDefault() {

        Map<String, String> params = new HashMap<>();
        params.put("address.name", "testName");
        params.put("address.province", "上海市");
        params.put("address.city", "市辖区");
        params.put("address.district", "黄浦区");
        params.put("address.address", "宛平南路100号");
        params.put("address.postcode", "200000");
        params.put("address.mobile", "15888888888");
        params.put("address.isDefault", "true");

        Http.Response response = POST("/addresses", params);
        assertStatus(302, response);
        int size = Address.findByOrder(user).size();
        assertEquals(2, size);

    }

    @Test
    public void testUpdate() {
        String params = "address.name=testName&address.province=上海市&address.city=市辖区&address.district=黄浦区&address.address=宛平南路&address.postcode=200000&address.mobile=15812345678";

        Http.Response response = PUT("/addresses/" + address.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        address.refresh();
        assertEquals("testName", address.name);
        assertEquals("15812345678", address.mobile);
    }

    @Test
    public void testUpdateDefault() {
        address.isDefault = false;
        address.save();

        assertFalse(address.isDefault);

        Http.Response response = PUT("/addresses/" + address.id + "/default", "application/x-www-form-urlencoded", "");
        assertStatus(302, response);
        address.refresh();
        assertTrue(address.isDefault);
    }

    @Test
    public void testDelete() {
        int oldSize = Address.findAll().size();

        Http.Response response = DELETE("/addresses/" + address.id);
        assertStatus(302, response);

        int newSize = Address.findAll().size();
        assertEquals(oldSize - 1, newSize);
    }

}
