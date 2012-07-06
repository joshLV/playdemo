package models.job.resaler;

import models.accounts.AccountType;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import play.jobs.Every;
import play.jobs.Job;

import java.math.BigDecimal;

/**
 * @author likang
 */
//@Every("1m")
public class YihaodianNewOrders extends Job {
    @Override
    public void doJob() throws NotEnoughInventoryException{
        Resaler resaler = null;
        Goods goods = null;
        long number = 1;
        String mobile = "";
        String extOrderId= "";

        Order order = Order.find("byExtOrderId", extOrderId).first();

        if(order == null){
            order = makeOrder(resaler, goods, number, mobile);
        }else {
            if(order.status == OrderStatus.UNPAID){
                order.payAndSendECoupon();
            }
        }
        markSent(order);
    }

    public Order makeOrder(Resaler resaler, Goods goods, long number, String mobile) throws NotEnoughInventoryException{
        Order order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
        order.deliveryType = DeliveryType.SMS;

        BigDecimal salePrice = BigDecimal.ZERO;
        BigDecimal resalerPrice = BigDecimal.ZERO;

        order.addOrderItem(goods, number, mobile, salePrice, resalerPrice);
        return order;
    }

    public void markSent(Order order){

    }
}
