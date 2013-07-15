package jobs.baidu;

import models.baidu.BaiduOrderMessageUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import play.jobs.Every;

import java.util.List;

/**
 * User: yan
 * Date: 13-7-12
 * Time: 下午9:54
 */
@JobDefine(title = "百度订单扫描", description = "处理OuterOrder中未生成的订单，生成券并发送")
@Every("1mn")
public class BaiduOrderJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<OuterOrder> orders = OuterOrder.find("partner = ? and ( status = ? or status = ?)",
                OuterOrderPartner.BD, OuterOrderStatus.ORDER_COPY, OuterOrderStatus.ORDER_DONE).fetch();
        for (OuterOrder order : orders) {
            BaiduOrderMessageUtil.send(order.orderId);
        }
    }
}
