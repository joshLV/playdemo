package models.order;

import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-5
 * Time: 上午11:27
 */
@Entity
@Table(name = "logistic")
public class Logistic extends Model {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    public OrderItems orderItems;

    /**
     * 第三方商品NO
     */
    @Transient
    public String outerGoodsNo;

    /**
     * 第三方订单编号
     */
    @Transient
    public String outerOrderNo;

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
     * 选项
     */
    @Column(name = "options")
    public String options;

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
