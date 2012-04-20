package models.mail;

import java.io.Serializable;
import java.util.List;

public class CouponMessage implements Serializable {

    private String email;
    
    private String fullName;
    
    private List<String> coupons;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<String> coupons) {
        this.coupons = coupons;
    }

    
}
