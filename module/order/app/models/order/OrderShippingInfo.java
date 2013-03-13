package models.order;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单发货信息表.
 * User: yanjy
 * Date: 13-3-5
 * Time: 上午11:27
 * <p/>
 */
@Entity
@Table(name = "order_shipping_info")
public class OrderShippingInfo extends Model {
    /**
     * 一个发货信息可能会有多个订单商品.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shippingInfo")
    @OrderBy("id")
    public List<OrderItems> orderItems;

    @Column(name = "outer_order_id")
    public String outerOrderId;

    /**
     * 单价
     */
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    /**
     * 购买数量
     */
    @Column(name = "buy_number")
    public Integer buyNumber;

    /**
     * 备注
     */
    @Column(name = "remarks")
    public String remarks;

    /**
     * 快递信息
     */
    @Column(name = "express_info")
    public String expressInfo;

    /**
     * 收件人
     */
    @Column(name = "receiver")
    public String receiver;

    /**
     * 手机
     */
    @Column(name = "phone")
    public String phone;

    /**
     * 固定电话
     */
    @Column(name = "tel")
    public String tel;

    /**
     * 付款时间
     */
    @Column(name = "paid_at")
    public Date paidAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 收货地址
     */
    @Column(name = "address")
    public String address;
    /**
     * 邮政
     */
    @Column(name = "zip_code")
    public String zipCode;

    /**
     * 发票抬头
     */
    @Column(name = "invoice_title")
    public String invoiceTitle;

    /**
     * 发货时间
     */
    @Column(name = "sent_at")
    public Date sentAt;

    /**
     * 物流公司
     */
    @Column(name = "express_company")
    public ExpressCompany expressCompany;

    /**
     * 物流单号
     */
    @Column(name = "express_number")
    public String expressNumber;

    /**
     * 已发货文件上传渠道时间，这里认为是下载渠道已发货文件的时间。
     */
    public Date uploadedAt;

}
