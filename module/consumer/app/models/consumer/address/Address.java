package models.consumer.address;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "address")
public class Address extends Model {
    @Column(name = "user_id")
    public long userId;
    public String province;
    public String city;
    public String district;
    public String address;
    public String name;
    public String postcode;
    public String phone;
    public String mobile;
    @Column(name = "is_default")
    public String isDefault;
    @Column(name = "create_at")
    public Date createdAt;
    @Column(name = "lock_version")
    public int lockVersion;
    @Column(name = "updated_at")
    public Date updatedAt;

    @Transient
    public String areaCode;
    @Transient
    public String phoneNumber;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;

    }

    public static List<Address> findByOrder(){
        return Address.find("order by is_default").fetch();
    }
}
