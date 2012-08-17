package models.job.resaler;

import models.accounts.AccountType;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import models.yihaodian.YihaodianJobUtil;
import models.yihaodian.YihaodianOrder;
import play.jobs.Job;

import java.math.BigDecimal;

/**
 * @author likang
 */
//@Every("1mn")
public class YihaodianNewOrders extends Job {
    @Override
    public void doJob() throws NotEnoughInventoryException{
        //todo
        Resaler resaler = null;
        Goods goods = null;
        int number = 1;
        String mobile = "";
        String extOrderId= "";
        //todo log everything

        YihaodianOrder yihaodianOrder = YihaodianOrder.find("byOrderNumber", extOrderId).first();
        if(yihaodianOrder == null){
            Order seewiOrder = buildSeewiOrder(resaler, goods, number, mobile);
            //发货
            seewiOrder.payAndSendECoupon();

            yihaodianOrder = new YihaodianOrder();
            //todo 填充一号店订单信息
            yihaodianOrder.seewiOrderId = seewiOrder.getId();
            yihaodianOrder.status = "CREATED";
            yihaodianOrder.pendingActions = "mark_send,";
            yihaodianOrder.save();

            //添加任务
            YihaodianJobUtil.addJob(yihaodianOrder.getId());
        }else {
            if(!yihaodianOrder.pendingActions.contains("mark_send")){
                yihaodianOrder.pendingActions = yihaodianOrder.pendingActions + "mark_send,";
                yihaodianOrder.save();
            }
        }
    }

    public Order buildSeewiOrder(Resaler resaler, Goods goods, int number, String mobile) throws NotEnoughInventoryException{
        Order order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        order.deliveryType = DeliveryType.SMS;

        //todo 创建订单
        BigDecimal salePrice = BigDecimal.ZERO;
        BigDecimal resalerPrice = BigDecimal.ZERO;

        order.addOrderItem(goods, number, mobile, salePrice, resalerPrice);
        order.createAndUpdateInventory();
        return order;
    }
}
