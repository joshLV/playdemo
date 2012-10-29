package models.consumer;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import play.Play;
import play.cache.Cache;
import play.db.jpa.Model;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

@Entity
@Table(name="user_web_identifications")
public class UserWebIdentification extends Model {

    private static final long serialVersionUID = 18232060911893921L;
    
    public static final String MQ_KEY = "WUI2NEW-";

    @Column(name="cookie_id", unique=true)
    public String cookieId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    public User user;

    @Column(name = "referer", length=4000)
    public String referer;

    @Column(name="referer_host")
    public String refererHost;

    @Column(name = "created_at")
    public Date createdAt;
    
    @Column(name = "first_page", length=4000)
    public String firstPage;

    @Column(name = "ip")
    public String ip;
    
    @Column(name="user_agent", length=512)
    public String userAgent;

    /**
     * 推荐码.
     * 第一个入站请求中的tj参数，记录为referCode.
     */
    @Column(name = "refer_code")
    public String referCode;

    /**
     * 加入购物车的数量
     */
    @Column(name = "cart_count")
    public Integer cartCount;

    /**
     * 成功支付的订单数量
     */
    @Column(name = "order_count")
    public Integer orderCount;
    
    /**
     * 注册数
     */
    @Column(name = "register_count")
    public Integer registerCount;
    
    /**
     * 成功支付的金额.
     */
    @Column(name = "pay_amount")
    public BigDecimal payAmount;

    /**
     * 按cookie值找到跟踪的值.
     * @param cookieValue
     * @return
     */
    public static UserWebIdentification findOne(String cookieValue) {
        return UserWebIdentification.find("cookieId=?", cookieValue).first();
    }
    
    @Transient
    @JsonIgnore
    public Long getSavedId() {
    	if (this.id == null) {
    		UserWebIdentification uwi = UserWebIdentification.findOne(this.cookieId);
    		if (uwi != null) {
    			return uwi.id;
    		}
    		this.save();
    	}
    	return this.id;
    }
    
    /**
     * 把当前对象放到Cache中，同时发到MQ中，由MQ执行插入到数据库的操作.
     */
	public void sendToCacheOrSave() {			
		if (Play.runingInTestMode()) {
			save();
		} else {
			Cache.add(MQ_KEY + this.cookieId, this, "300mn");
		}
    }
	
	public void notifyMQSave() {
		if (!Play.runingInTestMode()) {
			RabbitMQPublisher.publish(MQ_KEY, this);
		}
	}
}
