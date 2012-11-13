package factory.consumer;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.consumer.Address;
import models.consumer.User;

/**
 * 地址测试对象.
 * User: wangjia
 * Date: 12-8-23
 * Time: 下午5:37
 */
public class AddressFactory extends ModelFactory<Address> {
    @Override
    public Address define() {
        Address address = new Address();
//        User user = FactoryBoy.create(User.class);
//        address.user=user;
        address.province = "上海市";
        address.district = "市辖区";
        address.city = "黄浦区";
        address.postcode = "123456";
        address.mobile = "13764081569";
        address.address = "test3";
        address.isDefault = true;
        address.name = "add1";
        address.user = FactoryBoy.lastOrCreate(User.class);
//        System.out.println("useru11111<<<>>>>"+user);
        return address;
    }

    @Factory(name = "notDefault")
    public Address defineWithNotDefault(Address address) {
        address = new Address();
        address.province = "上海市";
        address.district = "市辖区";
        address.city = "黄浦区";
        address.postcode = "123456";
        address.mobile = "13764081569";
        address.address = "test3";
        address.isDefault = false;
        address.name = "add1";
        return address;
    }


}
