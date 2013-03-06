package models.order;

import com.google.gson.Gson;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import play.Logger;

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
    public String outerGoodsNo;

    /**
     * 第三方订单编号
     */
    public String outerOrderNo;

    /**
     * 单价
     */
    public BigDecimal salesPrice;

    /**
     * 购买数量
     */
    public Integer buyNumber;


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
    public String paidAt;

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

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
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

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
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

    /**
     * 转换为 OuterOrder
     *
     * @param partner 分销伙伴
     * @return OuterOrder
     */
    public OuterOrder toOuterOrder(OuterOrderPartner partner) throws Exception {
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, this.outerOrderNo).first();
        if (outerOrder != null) {
            throw new Exception();
        }

        outerOrder = new OuterOrder();
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
    public Order toYbqOrder(OuterOrderPartner partner) {
        Resaler resaler = Resaler.findOneByLoginName(partner.partnerLoginName());
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", partner.partnerLoginName());
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.id, AccountType.RESALER).save();
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(Long.valueOf(outerGoodsNo), partner);
        if (goods == null) {
            Logger.info("goods not found: %s,", outerGoodsNo);
            return null;
        }
        try {
            ybqOrder.addOrderItem(goods, buyNumber, phone, salesPrice, salesPrice).save();
        } catch (NotEnoughInventoryException e) {
        }
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
