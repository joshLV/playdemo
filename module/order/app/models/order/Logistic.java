package models.order;

import com.google.gson.Gson;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    static String [] dateFormats = new String[] {
            "yyyy-MM-dd HH:mm",// 淘宝
            "yyyy/MM/dd HH:mm",// 京东
    };

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


    public String getOuterGoodsNo() {
        return outerGoodsNo;
    }

    public void setOuterGoodsNo(String outerGoodsNo) {
        this.outerGoodsNo = outerGoodsNo;
    }

    public String getOuterOrderNo() {
        return outerOrderNo;
    }

    public void setOuterOrderNo(String outerOrderNo) {
        this.outerOrderNo = outerOrderNo;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public String getInvoiceContent() {
        return invoiceContent;
    }

    public void setInvoiceContent(String invoiceContent) {
        this.invoiceContent = invoiceContent;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setSalePrice(BigDecimal salesPrice) {
        this.salePrice = salesPrice;
    }

    public Integer getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(Integer buyNumber) {
        this.buyNumber = buyNumber;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getExpressInfo() {
        return expressInfo;
    }

    public void setExpressInfo(String expressInfo) {
        this.expressInfo = expressInfo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaidAtStr() {
        return paidAtStr;
    }

    public void setPaidAtStr(String paidAtStr) {
        if (paidAtStr.indexOf("结:") > -1) {
            paidAtStr = paidAtStr.substring(paidAtStr.indexOf("结") + 2);
        }

        this.paidAtStr = paidAtStr;
        this.paidAt = guessDate(paidAtStr);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    private Date guessDate(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        Double dateNumber = -1D;
        boolean isDouble = true;
        try {
            dateNumber = Double.parseDouble(date);
        } catch (NumberFormatException e) {
            isDouble = false;
        }

        if (isDouble) {
            //如果是double类型的，就看作是excel中格式化为日期的数字
            return convertExcelDate(dateNumber);
        } else {
            //否则猜测是正常的日期，只是格式各有不同，因此猜一下
            for (String format : dateFormats) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                try {
                    return dateFormat.parse(date);
                } catch (ParseException e) {
                    continue;
                }
            }
            return null;
        }
    }

    private Date convertExcelDate(Double dateNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, 1900);

        //excel中，数字1 代表 1900年1月1日，并且，excel认为1900年2月有29天，其实没有。所以我们在这里减2
        calendar.add(Calendar.DAY_OF_YEAR, dateNumber.intValue() - 2);
        dateNumber = (dateNumber - dateNumber.intValue()) * 24;
        calendar.add(Calendar.HOUR_OF_DAY, dateNumber.intValue());
        dateNumber = (dateNumber - dateNumber.intValue()) * 60;
        calendar.add(Calendar.MINUTE, dateNumber.intValue());

        dateNumber = (dateNumber - dateNumber.intValue()) * 60;
        calendar.add(Calendar.SECOND, (int) Math.round(dateNumber));

        return calendar.getTime();
    }

    /**
     * 转换为 OuterOrder
     *
     * @param partner 分销伙伴
     * @return OuterOrder
     */
    public OuterOrder toOuterOrder(OuterOrderPartner partner) {
        OuterOrder outerOrder = new OuterOrder();
        outerOrder.orderId = outerOrderNo;
        outerOrder.partner = partner;
        outerOrder.message = new Gson().toJson(this);
        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
        outerOrder.orderType = OuterOrderType.IMPORT;
        return outerOrder;
    }

    /**
     * 转换为一百券订单
     *
     * @param partner 分销伙伴
     */
    public Order toYbqOrder(OuterOrderPartner partner) throws NotEnoughInventoryException {
        Resaler resaler = Resaler.findOneByLoginName(partner.partnerLoginName());
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", partner.partnerLoginName());
            return null;
        }
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(outerGoodsNo, partner);
        if (goods == null) {
            Logger.info("goods not found: %s,", outerGoodsNo);
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.id, AccountType.RESALER).save();
        ybqOrder.addOrderItem(goods, buyNumber, phone, salePrice, salePrice).save();
        ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.paid();
        ybqOrder.save();
        return ybqOrder;

    }
}
