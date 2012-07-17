package models.consumer;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import play.db.jpa.Model;

@Entity
@Table(name="user_web_identifications")
public class UserWebIdentification extends Model {
    
    private static final long serialVersionUID = 18232060911893921L;
    
    @Column(name="cookie_id")
    public String cookieId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = true)
	public User user;
	
	public String referer;
	
	@Column(name="referer_host")
	public String refererHost;

    @Column(name = "created_at")
    public Date createdAt;
    
    @Column(name = "first_page")
    public String firstPage;
    
    /**
     * 推荐码.
     * 第一个入站请求中的tj参数，记录为referCode.
     */
    @Column(name = "refer_code")
    public String referCode;
    
    /**
     * 按用户id和cookie值找到跟踪的值.
     * @param cookieValue
     * @return
     */
    public static UserWebIdentification findOne(String cookieValue) {
        return UserWebIdentification.find("cookieId=?", cookieValue).first();
    }
}
