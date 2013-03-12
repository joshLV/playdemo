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
 * 导入实物订单信息的中间数据类.
 */
public class LogisticImportData {
    static String [] dateFormats = new String[] {
            "yyyy-MM-dd HH:mm",// 淘宝
            "yyyy/MM/dd HH:mm",// 京东
    };

    public OrderItems orderItems;

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
    public BigDecimal salePrice;

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
     * 固定电话
     */
    public String tel;

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
     * 邮政编码
     */
    public String zipCode;

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
