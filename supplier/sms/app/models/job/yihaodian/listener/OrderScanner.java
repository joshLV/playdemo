package models.job.yihaodian.listener;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.yihaodian.YihaodianQueueUtil;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * @author likang
 *         Date: 12-9-7
 */
@Every("1mn")
public class OrderScanner extends Job {
    @Override
    public void doJob() {
        List<OuterOrder> orders = OuterOrder.find("partner = ? and ( status = ? or status = ?)",
                OuterOrderPartner.YHD, OuterOrderStatus.ORDER_COPY, OuterOrderStatus.ORDER_DONE).fetch();
        for (OuterOrder order : orders){
            YihaodianQueueUtil.addJob(order.orderId);
        }
    }
}
