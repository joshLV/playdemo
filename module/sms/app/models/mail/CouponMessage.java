package models.mail;

import java.io.Serializable;
import java.util.List;

public class CouponMessage extends MailMessage implements Serializable {

    private String mailUrl;
    
    public String getMailUrl() {
		return mailUrl;
	}

	public void setMailUrl(String mailUrl) {
		this.mailUrl = mailUrl;
	}

	private List<String> coupons;

    public List<String> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<String> coupons) {
        this.coupons = coupons;
    }

    
}
