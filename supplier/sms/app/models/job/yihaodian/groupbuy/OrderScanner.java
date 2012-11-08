package models.job.yihaodian.groupbuy;

import models.order.OuterOrder;
import models.order.OuterOrderStatus;
import models.yihaodian.YHDGroupBuyMessage;
import models.yihaodian.YihaodianQueueUtil;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * @author likang
 *         Date: 12-9-16
 */
@Every("1mn")
public class OrderScanner extends Job{
    @Override
    public void doJob() {
        if(Play.runingInTestMode()){
            return;
        }
        List<OuterOrder> orders = OuterOrder.find("status = ? or status = ?",
                OuterOrderStatus.ORDER_DONE,
                OuterOrderStatus.REFUND_DONE).fetch();
        for (OuterOrder order : orders){
            YHDGroupBuyMessage message = new YHDGroupBuyMessage(order.orderNumber);
            YihaodianQueueUtil.addGroupBuyJob(message);
        }
    }
}
