package unit;

import models.consumer.Address;
import models.consumer.User;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

import java.util.List;

/**
 * 地址对象的测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 3:38 PM
 */
public class AddressUnitTest extends UnitTest {
    User user;
    List<Address> addresses;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        addresses = FactoryBoy.batchCreate(2, Address.class,
                new SequenceCallback<Address>() {
                    @Override
                    public void sequence(Address target, int seq) {
                        target.address = "Test#" + seq;
                    }
                });
    }

    @Test
    public void testGetFullAddress() {
        addresses.get(0).province = "上海";
        addresses.get(0).city = "上海";
        addresses.get(0).district = "浦东新区";
        addresses.get(0).address = "凌兆路";
        String fullAddress = addresses.get(0).getFullAddress();
        assertEquals("上海 上海 浦东新区 凌兆路", fullAddress);
    }

    @Test
    public void testFindDefault() {
        Address testAddress = Address.findDefault(user);
        assertEquals(addresses.get(0).address, testAddress.address);
    }

    @Test
    public void testFindByOrder() {
        List<Address> addressList = Address.findByOrder(user);
        assertEquals(2, addressList.size());
        assertEquals(addresses.get(0).address, addressList.get(0).address);
        assertEquals(addresses.get(1).address, addressList.get(1).address);
    }

    @Test
    public void testUpdateToUnDefault() {
        Address.updateToUnDefault(user);
        Address testAddress = Address.findDefault(user);
        assertNull(testAddress);
    }

    @Test
    public void testGetPhone() {
        Address testAddress = new Address();
        testAddress.areaCode = "021";
        testAddress.phoneNumber = "1234567";
        testAddress.phoneExtNumber = "123";
        testAddress.mobile = "13412341234";
        assertEquals("021-1234567-123", testAddress.getPhone());
    }

    @Test
    public void testUpdateDefault() {
        addresses.get(0).isDefault = false;
        addresses.get(0).save();
        addresses.get(0).refresh();
        assertEquals(false, addresses.get(0).isDefault);
        Address.updateDefault(addresses.get(0).id, user);
        Address testAddress = Address.findById(addresses.get(0).id);
        assertEquals(true, testAddress.isDefault);
    }


    @Test
    public void testDelete() {
        Address.delete(addresses.get(1).id, user);
        Address testAddress = Address.findById(addresses.get(1).id);
        assertNull(testAddress);
        testAddress = Address.findById(addresses.get(0).id);
        assertEquals(true, testAddress.isDefault);
    }


}
