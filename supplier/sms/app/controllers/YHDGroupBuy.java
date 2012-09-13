package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.api.order.YHDGroupBuyOrder;
import models.api.order.YHDGroupBuyOrderStatus;
import models.order.*;
import models.resale.Resaler;
import models.sales.MaterialType;
import models.yihaodian.OrderItem;
import models.sales.Goods;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-12
 */
public class YHDGroupBuy extends Controller{
    public static String YHD_LOGIN_NAME = Play.configuration.getProperty("yihaodian.resaler_login_name", "yihaodian");
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static void inform(String orderCode, Long productId, Integer productNum, BigDecimal orderAmount,
                              Date createTime, Date paidTime, String userPhone, BigDecimal productPrice,
                              Long groupId, String outerGroupId) {
        int errorCount = 0;
        if(orderCode == null){ errorCount += 1; }
        if(productNum == null) { errorCount += 1; }
        if(userPhone == null || userPhone.trim().equals("")) { errorCount += 1; }
        if(productPrice == null) { errorCount += 1; }
        if(outerGroupId == null) { errorCount += 1; }
        if(errorCount > 0){
            renderXml(informResult(errorCount, 0));
            return;
        }
        YHDGroupBuyOrder yhdGroupBuyOrder = new YHDGroupBuyOrder();
        yhdGroupBuyOrder.orderCode = orderCode;
        yhdGroupBuyOrder.productId = productId;
        yhdGroupBuyOrder.productNum = productNum;
        yhdGroupBuyOrder.orderAmount = orderAmount;
        yhdGroupBuyOrder.createTime = createTime;
        yhdGroupBuyOrder.paidTime = paidTime;
        yhdGroupBuyOrder.userPhone = userPhone;
        yhdGroupBuyOrder.productPrice = productPrice;
        yhdGroupBuyOrder.groupId = groupId;
        yhdGroupBuyOrder.outerGroupId = outerGroupId;
        yhdGroupBuyOrder.save();
        JPA.em().flush();

        try{
            JPA.em().refresh(yhdGroupBuyOrder, LockModeType.PESSIMISTIC_WRITE);
        }catch (PersistenceException e){
            //没拿到锁 放弃
            renderXml(informResult(1, 0));
            return;
        }

        Order ybqOrder = createYbqOrder(yhdGroupBuyOrder);
        if(ybqOrder != null){
            yhdGroupBuyOrder.status = YHDGroupBuyOrderStatus.PROCESSED;
            yhdGroupBuyOrder.ybqOrderId = ybqOrder.getId();
            yhdGroupBuyOrder.save();
        }
        renderXml(informResult(0, 1));
    }

    // 创建一百券订单
    public static Order createYbqOrder(YHDGroupBuyOrder yhdGroupBuyOrder) {
        Resaler resaler = Resaler.findOneByLoginName(YHD_LOGIN_NAME);
        if (resaler == null){
            Logger.error("can not find the resaler by login name: %s", YHD_LOGIN_NAME);
            return null;
        }
        Order ybqOrder = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        try {
            Goods goods = Goods.find("byId", yhdGroupBuyOrder.outerGroupId).first();
            if(goods == null){
                Logger.info("goods not found: %s", yhdGroupBuyOrder.outerGroupId );
                return null;
            }

            OrderItems uhuilaOrderItem  = ybqOrder.addOrderItem(
                    goods,
                    yhdGroupBuyOrder.productNum,
                    yhdGroupBuyOrder.userPhone,
                    yhdGroupBuyOrder.productPrice,
                    yhdGroupBuyOrder.productPrice );
            uhuilaOrderItem.save();
            if(goods.materialType.equals(MaterialType.REAL)){
                ybqOrder.deliveryType = DeliveryType.SMS;
            }else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            }
        } catch (NotEnoughInventoryException e) {
            Logger.info("enventory not enough");
            return null;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        return ybqOrder;
    }

    private static Document informResult(int errorCount, int updateCount){
        Document doc = makeDocument();
        Element rootElement = doc.createElement("response");
        doc.appendChild(rootElement);

        Element errorCountNode = doc.createElement("errorCount");
        errorCountNode.appendChild(doc.createTextNode(String.valueOf(errorCount)));
        rootElement.appendChild(errorCountNode);

        Element updateCountNode = doc.createElement("updateCount");
        updateCountNode.appendChild(doc.createTextNode(String.valueOf(updateCount)));
        rootElement.appendChild(updateCountNode);
        return doc;
    }

    private static Document makeDocument(){
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            return null;
        }
        return docBuilder.newDocument();
    }

    public static void voucherInfo(String orderCode, String partnerOrderCode){
        YHDGroupBuyOrder yhdGroupBuyOrder = YHDGroupBuyOrder.find("byOrderCode").first();
        if(yhdGroupBuyOrder == null){

            return;
        }
        Order ybqOrder = Order.findById(yhdGroupBuyOrder.ybqOrderId);
        if(ybqOrder == null){

            return;
        }
        Document doc = makeDocument();
        Element root = doc.createElement("response");
        doc.appendChild(root);

        Element errorCountNode = doc.createElement("errorCount");
        errorCountNode.appendChild(doc.createTextNode(String.valueOf(0)));
        root.appendChild(errorCountNode);

        Element totalCountNode = doc.createElement("totalCount");
        totalCountNode.appendChild(doc.createTextNode(String.valueOf(ybqOrder.orderItems.size())));
        root.appendChild(totalCountNode);

        Element voucherInfoListNode = doc.createElement("voucherInfoList");
        root.appendChild(voucherInfoListNode);

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

        List<ECoupon> coupons = ECoupon.find("byOrder", ybqOrder).fetch();


        for(ECoupon coupon : coupons){
            Element voucherInfoNode = doc.createElement("voucherInfo");

            Element issueTimeNode = doc.createElement("issueTime");
            issueTimeNode.appendChild(doc.createTextNode(format.format(coupon.createdAt)));
            voucherInfoNode.appendChild(issueTimeNode);

            Element voucherCodeNode = doc.createElement("voucherCode");
            voucherCodeNode.appendChild(doc.createTextNode(coupon.eCouponSn));
            voucherInfoNode.appendChild(voucherCodeNode);

            Element voucherCountNode = doc.createElement("voucherCount");
            voucherCountNode.appendChild(doc.createTextNode("1"));
            voucherInfoNode.appendChild(voucherCountNode);

            Element voucherStartTimeNode = doc.createElement("voucherStartTime");
            if(coupon.effectiveAt == null){
                coupon.effectiveAt = new Date();
            }
            voucherStartTimeNode.appendChild(doc.createTextNode(format.format(coupon.effectiveAt)));
            voucherInfoNode.appendChild(voucherStartTimeNode);

            Element voucherEndTimeNode = doc.createElement("voucherEndTime");
        }

    }

    private class Response{
        private int errorCount;
        private int totalCount;
        private List<VoucherInfo> voucherInfoList;

        public Response(){}
    }

    private class VoucherInfo{
        private String issueTime;
        private String voucherCode;
        private int voucherCount;
        private String voucherEndTime;
        private String voucherStartTIme;

        public VoucherInfo(){}
    }


}


