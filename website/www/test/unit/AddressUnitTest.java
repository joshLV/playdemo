package unit;

import models.consumer.Address;
import models.consumer.User;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

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

    @Test
    public void testFindByOrder() {
        User user = new User();
        user.id = (Long) Fixtures.idCache.get("models.consumer.User-User1");
        List<Address> addressList = Address.findByOrder(user);
        assertEquals(2, addressList.size());
        assertEquals("test2", addressList.get(0).address);
        assertEquals("test1", addressList.get(1).address);
    }


    @Test
    public void testUpdateToUnDefault() {
        User user = new User();
        user.id = (Long) Fixtures.idCache.get("models.consumer.User-User1");
        Address.updateToUnDefault(user);

        Address address = Address.findDefault(user);
        assertNull(address);
    }

    @Test
    public void testGetPhone() {
        Address address = new Address();
        address.areaCode = "021";
        address.phoneNumber = "1234567";
        address.phoneExtNumber = "123";
        address.mobile = "13412341234";
        assertEquals("13412341234 021-1234567-123", address.getPhone());
    }

    @Test
    public void testUpdateDefault() {
        Long id = (Long) Fixtures.idCache.get("models.consumer.Address-test1");
        Address address = Address.findById(id);
        assertEquals(false, address.isDefault);
        User user = new User();
        user.id = (Long) Fixtures.idCache.get("models.consumer.User-User1");
        Address.updateDefault(id, user);
        address = Address.findById(id);
        assertEquals(true, address.isDefault);
    }

    @Test
    public void testDelete() {
        long id = (Long) Fixtures.idCache.get("models.consumer.Address-test2");
        Address.delete(id);
        Address address = Address.findById(id);
        assertNull(address);
        Long defaultId = (Long) Fixtures.idCache.get("models.consumer.Address-test1");
        address = Address.findById(defaultId);
        assertEquals(true, address.isDefault);
    }


}
