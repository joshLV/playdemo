package models.job.yihaodian.listener;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.yihaodian.YihaodianQueueUtil;
import play.jobs.Every;

import java.util.List;

/**
 * @author likang
 *         Date: 12-9-7
 */
@JobDefine(title = "一号店订单扫描", description = "处理OuterOrder中未生成的订单，生成券并发送")
@Every("1mn")
public class OrderScanner extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<OuterOrder> orders = OuterOrder.find("partner = ? and ( status = ? or status = ?)",
                OuterOrderPartner.YHD, OuterOrderStatus.ORDER_COPY, OuterOrderStatus.ORDER_DONE).fetch();
        for (OuterOrder order : orders) {
            YihaodianQueueUtil.addJob(order.orderId);
        }
    }
}
