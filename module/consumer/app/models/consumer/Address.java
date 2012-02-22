package models.consumer;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "address")
public class Address extends Model {
    @ManyToOne
    public User user;
    public String province;
    public String city;
    public String district;
    public String address;
    public String name;
    public String postcode;

    public String mobile;
    @Column(name = "is_default")
    public String isDefault;
    @Column(name = "create_at")
    public Date createdAt;
    @Column(name = "lock_version")
    public int lockVersion;
    @Column(name = "updated_at")
    public Date updatedAt;
    @Column(name = "area_code")
    public String areaCode;
    @Column(name = "phone_number")
    public String phoneNumber;
    @Column(name = "phone_ext_number")
    public String phoneExtNumber;

    @Transient
    public String getFullAddress() {
        String fullAddress = "";
        if (!"ALL".equals(province) && !"".equals(province)) {
            fullAddress += province;
        }
        if (!"ALL".equals(city) && !"".equals(city)) {
            fullAddress += " " + city;
        }
        if (!"ALL".equals(district) && !"".equals(district)) {
            fullAddress += " " + district;
        }
        if (!"".equals(fullAddress)) {
            fullAddress += " ";
        }
        if (!"".equals(address)) {
            fullAddress += address;
        }
        return fullAddress;

    }

    @Transient
    public String getPhone() {
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
        if (mobile != null && !mobile.equals("")) {
            return mobile + " " + phoneStr.toString();
        }
        return phoneStr.toString();
    }

    public static List<Address> findByOrder(User user) {
        return Address.find("user=? order by isDefault",user).fetch();
    }

    public static void updateToUnDefault(User user) {
        List<Address> addressList = Address.find("byUserAndIsDefault", user, "true").fetch();
        for (Address address : addressList) {
            address.isDefault = "false";
            address.save();
        }
    }

    public static Address findDefault(User user) {
        return Address.find("byUserAndIsDefault", user, "true").first();
    }
}
