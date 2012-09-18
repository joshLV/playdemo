package models.job.yihaodian.groupbuy;

import models.yihaodian.YHDGroupBuyMessage;
import models.yihaodian.YihaodianQueueUtil;
import models.yihaodian.groupbuy.YHDGroupBuyOrder;
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
        List<YHDGroupBuyOrder> orders = YHDGroupBuyOrder.find("jobFlag = ? or jobFlag = ?",
                YHDGroupBuyOrderJobFlag.ORDER_DONE, YHDGroupBuyOrderJobFlag.REFUND_DONE).fetch();
        for (YHDGroupBuyOrder order : orders){
            YHDGroupBuyMessage message = new YHDGroupBuyMessage(order.orderCode);
            YihaodianQueueUtil.addGroupBuyJob(message);
        }
    }
}
