package models.order;

import javax.persistence.Column;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-5
 * Time: 上午11:27
 * <p/>
 * TODO: 将重构为OrderShippingInfo
 */
@Entity
@Table(name = "logistic_info")
public class Logistic extends Model {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    @OrderBy("id")
    public List<OrderItems> orderItems;

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
    @Transient
    public String paidAtStr;

    @Transient
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
    @Column(name = "invoice_content")
    public String invoiceContent;

    /**
     * 物流公司
     */
    @Column(name = "express_company")
    public String expressCompany;

    /**
     * 物流单号
     */
    @Column(name = "express_number")
    public String expressNumber;

}
