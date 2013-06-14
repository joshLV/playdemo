package models.consumer;

import models.mq.QueueIDMessage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class UserWebIdentificationData extends QueueIDMessage implements Serializable {

    private static final long serialVersionUID = 197132060911893921L;

    public static final String MQ_KEY = "website.identification";
    
    public String cookieId;

    public String referer;

    public String refererHost;

    public Date createdAt;
    
    public String firstPage;

    public String ip;
    
    public String userAgent;

    /**
     * 推荐码.
     * 第一个入站请求中的tj参数，记录为referCode.
     */
    public String referCode;

    /**
     * 加入购物车的数量
     */
    public Integer cartCount;

    /**
     * 成功支付的订单数量
     */
    public Integer orderCount;
    
    /**
     * 注册数
     */
    public Integer registerCount;
    
    /**
     * 成功支付的金额.
     */
    public BigDecimal payAmount;

    public static UserWebIdentificationData from(UserWebIdentification uwi) {
    	UserWebIdentificationData data = new UserWebIdentificationData();
    	data.cartCount = uwi.cartCount;
    	data.cookieId = uwi.cookieId;
    	data.createdAt = uwi.createdAt;
    	data.firstPage = uwi.firstPage;
    	data.ip = uwi.ip;
    	data.orderCount = uwi.orderCount;
    	data.payAmount = uwi.payAmount;
    	data.referCode = uwi.referCode;
    	data.referer = uwi.referer;
    	data.refererHost = uwi.refererHost;
    	data.registerCount = uwi.registerCount;
    	data.userAgent = uwi.userAgent;
    	return data;
    }
    

    public UserWebIdentification toUserWebIdentification() {
    	UserWebIdentification uwi = new UserWebIdentification();
    	uwi.cartCount = this.cartCount;
    	uwi.cookieId = this.cookieId;
    	uwi.createdAt = this.createdAt;
    	uwi.firstPage = this.firstPage;
    	uwi.ip = this.ip;
    	uwi.orderCount = this.orderCount;
    	uwi.payAmount = this.payAmount;
    	uwi.referCode = this.referCode;
    	uwi.referer = this.referer;
    	uwi.refererHost = this.refererHost;
    	uwi.registerCount = this.registerCount;
    	uwi.userAgent = this.userAgent;
    	return uwi;
    }

    @Override
    public String getId() {
        return MQ_KEY + this.cookieId;
    }
}
