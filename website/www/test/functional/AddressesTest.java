/**
 *
 */
package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangjia
 * @date 2012-7-30 上午9:10:40
 */
public class AddressesTest extends FunctionalTest {
    UserInfo userInfo;
    User user;
    Address address;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        address = FactoryBoy.create(Address.class);

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/orders/addresses");
        assertStatus(200, response);
        assertContentMatch("收货地址", response);
        List<Address> testAddress = (List<Address>) renderArgs("addressList");
        assertEquals(address.id, testAddress.get(0).id);
    }

    @Test
    public void testCreate() {
        Long cnt = Address.count();
        assertEquals(1, cnt.intValue());
        Map<String, String> params = new HashMap();
        params.put("address.name", "上海浦东新区凌兆路");
        params.put("address.address", "凌兆路");
        params.put("address.postcode", "2000120");
        params.put("address.mobile", "134242424121");
        params.put("address.phoneNumber", "021");
        params.put("address.province", "上海");
        params.put("address.district", "浦东新区");
        params.put("address.city", "上海");

        Http.Response response = POST("/orders/addresses/new");
        assertStatus(200, response);
        assertContentMatch("收货地址", response);
        assertEquals(cnt + 1, Address.count());

    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/orders/addresses/new");
        assertStatus(200, response);
        assertContentMatch("请填写您的真实姓名", response);
    }

    @Test
    public void testList() {
        Http.Response response = GET("/orders/addresses/list");
        assertStatus(200, response);
        assertContentMatch("编辑", response);
        List<Address> testAddress = (List<Address>) renderArgs("addressList");
        assertEquals(address.id, testAddress.get(0).id);
    }

    @Test
    public void testShow() {
        Http.Response response = GET("/orders/addresses/" + address.id);
        assertStatus(200, response);
        assertContentMatch("收货地址", response);
        Address testAddress = (Address) renderArgs("address");
        assertEquals(address.id, testAddress.id);
    }

    @Test
    public void testShowDefault() {
        Http.Response response = GET("/orders/addresses/default");
        assertStatus(200, response);
        assertContentMatch("收货地址", response);
        Address testAddress = (Address) renderArgs("address");
        assertEquals(address.id, testAddress.id);
    }

    @Test
    public void testUpdateDefault() {
        String updatedName = "徐汇区";
        address.city = updatedName;
        Http.Response response = PUT("/orders/addresses/" + address.id + "/default", "application/x-www-form-urlencoded", address.city);
        assertStatus(200, response);
        address = Address.findById(address.id);
        assertEquals(updatedName, address.city);
    }

    @Test
    public void testDelete() {
        Address addressDeleted1 = Address.find("id=?", address.id).first();
        assertNotNull(addressDeleted1);
        assertEquals(1, Address.count());
        Response response = DELETE("/orders/addresses/" + address.id);
        assertStatus(200, response);
        assertEquals(0, Address.count());
        Address addressDeleted = Address.find("id=?", address.id).first();
        assertNull(addressDeleted);
    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/orders/addresses/" + address.id + "/edit");
        assertStatus(200, response);
        assertEquals(address, (Address) renderArgs("address"));
    }

    @Test
    public void testUpdate() {
        Http.Response response = PUT("/orders/addresses/" + address.id, "application/x-www-form-urlencoded", "address.id=" + address.id);
        assertStatus(200, response);
        assertEquals(address, (Address) renderArgs("address"));
    }

    @Test
    public void testUpdateNoDefault() {
        address.isDefault = null;
        address.save();
        address.refresh();
        Http.Response response = PUT("/orders/addresses/" + address.id, "application/x-www-form-urlencoded", "address.id=" + address.id);
        assertStatus(200, response);
        assertEquals(address, (Address) renderArgs("address"));
    }
}
