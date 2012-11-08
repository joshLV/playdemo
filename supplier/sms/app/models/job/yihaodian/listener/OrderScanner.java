package models.job.yihaodian.listener;

import models.yihaodian.shop.JobFlag;
import models.yihaodian.YihaodianJobMessage;
import models.yihaodian.shop.YihaodianOrder;
import models.yihaodian.YihaodianQueueUtil;
import play.Play;
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
        if(Play.runingInTestMode()){
            return;
        }
        List<YihaodianOrder> orders = YihaodianOrder.find("jobFlag = ? or jobFlag = ?",
                JobFlag.SEND_COPY, JobFlag.SEND_DONE).fetch();
        for (YihaodianOrder order : orders){
            YihaodianJobMessage message = new YihaodianJobMessage(order.orderId);
            YihaodianQueueUtil.addJob(message);
        }
    }
}
