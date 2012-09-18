package models.job.dangdang.listener;

import models.dangdang.*;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * @author yanjy
 *         Date: 12-9-17
 */
@Every("1mn")
public class DDOrderScanner extends Job {
    @Override
    public void doJob() {
        List<DDOrder> orders = DDOrder.find("status = ? or status= ?",
                DDOrderStatus.ORDER_ACCEPT, DDOrderStatus.ORDER_SEND).fetch();
        for (DDOrder order : orders) {
            DDOrderJobMessage message = new DDOrderJobMessage(order.orderId);
            DDOrderQueueUtil.addJob(message);
        }
    }
}
