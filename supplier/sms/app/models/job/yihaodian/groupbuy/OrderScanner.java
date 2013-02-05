package models.job.yihaodian.groupbuy;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
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
        List<OuterOrder> orders = OuterOrder.find("partner = ? and (status = ? or status = ?)",
                OuterOrderPartner.YHD,
                OuterOrderStatus.ORDER_DONE,
                OuterOrderStatus.REFUND_DONE).fetch();
        for (OuterOrder order : orders){
            YHDGroupBuyMessage message = new YHDGroupBuyMessage(order.orderId);
            YihaodianQueueUtil.addGroupBuyJob(message);
        }
    }
}
