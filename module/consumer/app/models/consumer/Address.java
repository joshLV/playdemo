package models.consumer;

import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;
import play.modules.view_ext.annotation.Postcode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "address")
public class Address extends Model {
    @ManyToOne
    public User user;
    @Required
    public String province;
    @Required
    public String city;
    @Required
    public String district;
    @Required
    public String address;
    @Required
    @MaxSize(20)
    public String name;
    @Required
    @Postcode
    public String postcode;

    @Mobile
    public String mobile;
    @Column(name = "is_default")
    public Boolean isDefault;
    @Column(name = "create_at")
    public Date createdAt;
    @Column(name = "lock_version")
    public int lockVersion;
    @Column(name = "updated_at")
    public Date updatedAt;
    @Column(name = "area_code")
    public String areaCode;
    @Column(name = "phone_number")
    @Match("^([0-9]+)$")
    public String phoneNumber;
    @Column(name = "phone_ext_number")
    @Match("^([0-9]+)$")
    public String phoneExtNumber;

    @Transient
    public String getFullAddress() {
        String fullAddress = getArea();
        if (!"".equals(fullAddress)) {
            fullAddress += " ";
        }
        if (!"".equals(address)) {
            fullAddress += address;
        }
        return fullAddress;
    }
    
    @Transient
    public String getArea(){
        String area = "";
        if (!"ALL".equals(province) && !"".equals(province)) {
            area += province;
        }
        if (!"ALL".equals(city) && !"".equals(city)) {
            area += " " + city;
        }
        if (!"ALL".equals(district) && !"".equals(district)) {
            area += " " + district;
        }
        return area;
    }

    @Transient
    public String getPhone() {
        String phoneStr = getNormalPhone();
        if (mobile != null && !mobile.equals("")) {
            return mobile + " " + phoneStr;
        }
        return phoneStr;
    }

    @Transient
    private String getNormalPhone() {
        StringBuilder phoneStr = new StringBuilder();
        if (areaCode != null && !areaCode.equals("")) {
            phoneStr.append(areaCode);
            phoneStr.append("-");
        }
        phoneStr.append(phoneNumber == null ? " " : phoneNumber);
        if (phoneExtNumber != null && !phoneExtNumber.equals("")) {
            phoneStr.append("-");
            phoneStr.append(phoneExtNumber);
        }
        return phoneStr.toString();
    }

    public static List<Address> findByOrder(User user) {
        return Address.find("user=? order by isDefault DESC", user).fetch();
    }

    public static void updateToUnDefault(User user) {
        List<Address> addressList = Address.find("byUserAndIsDefault", user, true).fetch();
        for (Address address : addressList) {
            address.isDefault = false;
            address.save();
        }
    }

    public static Address findDefault(User user) {
        return Address.find("byUserAndIsDefault", user, true).first();
    }

    public static void updateDefault(long id, User user) {
        Address address = Address.findById(id);
        if (address != null) {
            Address.updateToUnDefault(user);
            address.isDefault = true;
            address.save();
        }
    }


    public static void delete(long id) {
        Address address = Address.findById(id);
        if (address != null) {
            if (address.isDefault != null && address.isDefault) {
                address.delete();
                List<Address> addressList = Address.findAll();
                if (addressList.size() > 0) {
                    Address defaultAddress = addressList.get(0);
                    defaultAddress.isDefault = true;
                    defaultAddress.save();
                }
            } else {
                address.delete();
            }
        }
    }

    public static void update(Long id, Address address) {
        Address oldAddress = Address.findById(id);
        oldAddress.isDefault = address.isDefault;
        if (address.isDefault) {
            updateToUnDefault(oldAddress.user);
        }

        oldAddress.updatedAt = new Date();
        oldAddress.city = address.city;
        oldAddress.address = address.address;
        oldAddress.areaCode = address.areaCode;
        oldAddress.district = address.district;
        oldAddress.mobile = address.mobile;
        oldAddress.name = address.name;
        oldAddress.phoneExtNumber = address.phoneExtNumber;
        oldAddress.phoneNumber = address.phoneNumber;
        oldAddress.postcode = address.postcode;
        oldAddress.province = address.province;
        oldAddress.save();
    }


}
