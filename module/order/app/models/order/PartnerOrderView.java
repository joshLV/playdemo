package models.order;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-5
 * Time: 上午11:27
 */
public class PartnerOrderView {

    /**
     * 第三方商品NO
     */
    public Long outerGoodsNo;

    /**
     * 第三方订单编号
     */
    public Long outerOrderNo;

    /**
     * 单价
     */
    public BigDecimal salesPrice;

    /**
     * 购买数量
     */
    public Long buyNumber;


    /**
     * 选项
     */
    public String options;

    /**
     * 备注
     */
    public String remarks;

    /**
     * 快递信息
     */
    public String expressInfo;
    /**
     * 收件人
     */
    public String receiver;

    /**
     * 手机
     */
    public String phone;

    /**
     * 付款时间
     */
    public Date paidAt;

    /**
     * 创建时间
     */
    public Date createdAt;

    /**
     * 收货地址
     */
    public String address;
    /**
     * 邮政
     */
    public String zipCode;

    public Long getOuterGoodsNo() {
        return outerGoodsNo;
    }

    public void setOuterGoodsNo(Long outerGoodsNo) {
        this.outerGoodsNo = outerGoodsNo;
    }

    public Long getOuterOrderNo() {
        return outerOrderNo;
    }

    public void setOuterOrderNo(Long outerOrderNo) {
        this.outerOrderNo = outerOrderNo;
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
    }

    public Long getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(Long buyNumber) {
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
}
