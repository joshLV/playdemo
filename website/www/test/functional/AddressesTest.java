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

import java.util.List;

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

}
