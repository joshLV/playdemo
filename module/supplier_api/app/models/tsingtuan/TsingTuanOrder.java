package models.tsingtuan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import models.order.ECoupon;
import models.sales.Goods;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import play.Play;

public class TsingTuanOrder implements Serializable {
    
    private static final long serialVersionUID = 7063112063912831L;
    
    public static final String SECRET = Play.configuration.getProperty("tsingtuan.secret"); 
    
    // 券市场订单ID
    public Long orderId;
    
    // 对应清团产品ID
    public Long teamId;

    public String state = "pay";

    /**
     * 快递费用，默认为0
     */
    public BigDecimal fare = BigDecimal.ZERO;
  
    /**
     * 商品总价，不包括快递费用.
     */
    public BigDecimal money;
    
    /**
     * 总额.
     */
    public BigDecimal origin;
    
    public String address; // 顾客地址 非邮购为空
    
    public String zipcode; // 邮编 非邮购为空

    public String realname; // 顾客名称 非邮购为空
    
    public String mobile;  // 顾客手机号 

    public Integer quantity; // 商品数量 

    public String remark; // 顾客留言
    
    public String condbuy; // 用户购买选项 格式如：紫色@XL号@短款

    public Long create_time; // 订单创建时间 格式：时间戳 1285813236000

    public Long pay_time; //订单支付时间 格式：时间戳 1285813236000
    
    public String coupons; // 优惠券 格式：优惠券编号,券密码@券编号,密码@......

    public String password;
    
    /**
     * 签名 为了保证此接口不被恶意使用，需要提供加密保护，生成规则为：
     *     md5(order_id| team_id|origin|pay_time|secret);
     */
    private String sign;
    
    public static TsingTuanOrder from(ECoupon ecoupon) {
        
        Goods goods = ecoupon.goods;
        if (goods.getSupplier() == null || !"tsingtuan".equals(goods.getSupplier().domainName)) {
            return null;   // 不是清团的产品不会返回记录
        }
        TsingTuanOrder order = new TsingTuanOrder();
        order.orderId = ecoupon.id;
        order.teamId = goods.supplierGoodsId;
        order.money = ecoupon.salePrice;
        order.origin = ecoupon.salePrice;
        order.coupons = ecoupon.eCouponSn;
        order.mobile = ecoupon.orderItems.phone;
        order.quantity = 1;
        order.remark = ecoupon.order.remark;
        order.create_time = ecoupon.order.createdAt.getTime() / 1000l;
        order.pay_time = ecoupon.order.paidAt.getTime() / 1000l;
        
        // 如果是退款，则使用这个时间
        order.refund_time = new Date().getTime() / 1000l;
        
        return order;
    }

    /**
     * 生成小写的密码串。
     */
    @JsonIgnore
    public String getSign() {
        if (sign != null) {
            return sign;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.orderId).append("|")
              .append(this.teamId).append("|")
              .append(this.origin).append("|")
              .append(this.pay_time).append("|")
              .append(SECRET);
        sign = DigestUtils.md5Hex(sb.toString());
        System.out.println("sign=" + sign);
        return sign;
    }

    private String refundSign;

    public Long refund_time;
    
    @JsonIgnore
    public String getRefundSign() {
        if (refundSign != null) {
            return refundSign;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.orderId).append("|")
              .append(this.refund_time).append("|")
              .append(SECRET);
        refundSign = DigestUtils.md5Hex(sb.toString());
        System.out.println("refundSign=" + refundSign);
        return refundSign;
    }
}
