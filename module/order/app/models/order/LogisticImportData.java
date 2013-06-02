package models.order;

import com.google.gson.Gson;
import models.accounts.Account;
import models.accounts.PaymentSource;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入实物订单信息的中间数据类.
 */
public class LogisticImportData implements Cloneable {

    static String[] dateFormats = new String[]{
            "yyyy-MM-dd HH:mm",// 淘宝
            "yyyy/MM/dd HH:mm",// 京东
    };

    /**
     * 第三方商品NO
     */
    public String outerGoodsNo;

    /**
     * 第三方订单编号
     */
    public String outerOrderNo;

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    /**
     * 商品Code:上传发货单的时候用，和orderNumber一起作为主键查询一个orderItems
     */
    public String goodsCode;

    /**
     * 一百券orderNumber
     */
    public String orderNumber;

    /**
     * 单价
     */
    public BigDecimal salePrice;

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
     * 固定电话
     */
    public String tel;


    public String paidAtStr;

    /**
     * 付款时间
     */
    public Date paidAt;


    /**
     * 省市
     */
    public String province;
    /**
     * 收货地址
     */
    public String address;
    /**
     * 邮政编码
     */
    public String zipCode;

    public String getInvoiceTitle() {
        return invoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle) {
        this.invoiceTitle = invoiceTitle;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }


    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * 发票抬头
     */

    public String invoiceTitle;

    /**
     * 物流公司
     */
    public String expressCompany;

    /**
     * 物流单号
     */
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

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setSalePrice(BigDecimal salesPrice) {
        this.salePrice = salesPrice;
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
        if (phone.indexOf("E") >= 0) {
            try {
                Double p = Double.parseDouble(phone);
                DecimalFormat df = new DecimalFormat("#");
                this.phone = df.format(p);
                return;
            } catch (NumberFormatException e) {
                // ignore
                return;
            }
        }
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
        calendar.set(Calendar.YEAR, 1900);
        calendar.set(Calendar.DAY_OF_YEAR, 1);

        //excel中，数字1 代表 1900年1月1日，并且，excel认为1900年2月有29天，其实没有。所以我们在这里减2
        calendar.add(Calendar.DAY_OF_YEAR, dateNumber.intValue() - 2);
        dateNumber = (dateNumber - dateNumber.intValue()) * 24;
        calendar.set(Calendar.HOUR_OF_DAY, dateNumber.intValue());
        dateNumber = (dateNumber - dateNumber.intValue()) * 60;
        calendar.set(Calendar.MINUTE, dateNumber.intValue());

        dateNumber = (dateNumber - dateNumber.intValue()) * 60;
        calendar.set(Calendar.SECOND, (int) Math.round(dateNumber));

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
        if (partner == OuterOrderPartner.TB) {
            outerOrder.resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        } else {
            outerOrder.resaler = Resaler.findApprovedByLoginName(partner.partnerLoginName());
        }
        outerOrder.message = new Gson().toJson(this);
        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
        outerOrder.orderType = OuterOrderType.IMPORT;
        outerOrder.createdAt = new Date();
        return outerOrder;
    }


    /**
     * 转换为一百券订单
     *
     * @param partner 分销伙伴
     */
    public Order createYbqOrderByWB(OuterOrderPartner partner) {
        Resaler resaler = Resaler.findOneByLoginName(partner.partnerLoginName());
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", partner.partnerLoginName());
            return null;
        }
        return Order.createResaleOrder(resaler).save();

    }

    public OrderShippingInfo createOrderShipInfo() {
        // 生成OrderShippingInfo
        OrderShippingInfo orderShippingInfo = new OrderShippingInfo();
        orderShippingInfo.remarks = this.remarks;
        orderShippingInfo.expressInfo = this.expressInfo;
        orderShippingInfo.receiver = this.receiver;
        orderShippingInfo.phone = this.phone;
        orderShippingInfo.tel = this.tel;
        orderShippingInfo.paidAt = this.paidAt;
        orderShippingInfo.createdAt = new Date();
        orderShippingInfo.address = StringUtils.trimToEmpty(this.province) + this.address;
        orderShippingInfo.zipCode = this.zipCode;
        orderShippingInfo.invoiceTitle = this.invoiceTitle;
        orderShippingInfo.outerOrderId = this.outerOrderNo;
        orderShippingInfo.save();
        return orderShippingInfo;
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
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(resaler, outerGoodsNo, partner);
        if (goods == null) {
            Logger.info("goods not found: %s,", outerGoodsNo);
            return null;
        }
        Order ybqOrder = Order.createResaleOrder(resaler).save();

        // 生成OrderShippingInfo
        OrderShippingInfo orderShippingInfo = this.createOrderShipInfo();

        OrderItems orderItems = ybqOrder.addOrderItem(goods, buyNumber, phone, salePrice, salePrice);
        orderItems.shippingInfo = orderShippingInfo;
        orderItems.options = this.options;
        orderItems.outerGoodsNo = this.outerGoodsNo;
        orderItems.save();

        ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        Account account = ybqOrder.chargeAccount();
        ybqOrder.paid(account);
        ybqOrder.paidAt = this.paidAt;
        ybqOrder.save();
        return ybqOrder;
    }


    private static Pattern wubaGoodsPartern = Pattern.compile("^([^x]*)x(\\d+)$");

    /**
     * 拆分58团购导入的实体券.
     */
    public List<LogisticImportData> processWubaLogistic() {
        String outerGoodsNOs = this.outerGoodsNo;

        String[] outerGoodsLines = outerGoodsNOs.split("\n");
        List<LogisticImportData> logisticImportDataList = new ArrayList<>();
        for (String outerGoodsLine : outerGoodsLines) {
            if (StringUtils.isBlank(outerGoodsLine)) {
                continue;
            }
            outerGoodsLine = StringUtils.strip(outerGoodsLine);
            Matcher matcher = wubaGoodsPartern.matcher(outerGoodsLine);
            if (matcher.matches()) {
                try {
                    LogisticImportData data = (LogisticImportData) this.clone();
                    data.outerGoodsNo = matcher.group(1);
                    data.buyNumber = Long.parseLong(matcher.group(2));
                    logisticImportDataList.add(data);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        return logisticImportDataList;
    }

    public OrderItems createOrderItem(Order ybqOrder, Goods goods, OrderShippingInfo orderShippingInfo, Long buyNumber, BigDecimal salePrice) throws NotEnoughInventoryException {
        OrderItems orderItems = ybqOrder.addOrderItem(goods, buyNumber, phone, salePrice, salePrice);
        orderItems.shippingInfo = orderShippingInfo;
        orderItems.options = this.options;
        orderItems.outerGoodsNo = this.outerGoodsNo;
        orderItems.save();
        return orderItems;
    }
}
