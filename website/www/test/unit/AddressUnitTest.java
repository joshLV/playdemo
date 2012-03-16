package unit;

import models.consumer.Address;
import models.consumer.User;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * 地址对象的测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 3:38 PM
 */
public class AddressUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Address.class);
        Fixtures.delete(User.class);
        Fixtures.loadModels("fixture/users.yml");
        Fixtures.loadModels("fixture/addresses.yml");
    }

    @Test
    public void testGetFullAddress() {
        Address address = new Address();
        address.province = "上海";
        address.city = "上海";
        address.district = "浦东新区";
        address.address = "凌兆路";
        String fullAddress = address.getFullAddress();
        assertEquals("上海 上海 浦东新区 凌兆路", fullAddress);
    }

    @Test
    public void testFindDefault() {
        User user = new User();
        user.id = (Long) Fixtures.idCache.get("models.consumer.User-User1");
        Address address = Address.findDefault(user);
        assertEquals("test2", address.address);
    }

}
