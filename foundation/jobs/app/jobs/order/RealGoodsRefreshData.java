package jobs.order;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OrderItems;
import models.order.OrderStatus;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.jobs.On;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User: yan
 * Date: 13-6-25
 * Time: 下午5:30
 */
@JobDefine(title = "实物洗数据", description = "实物洗数据，给商户打款")
@On("0 0 4 * * ?")
public class RealGoodsRefreshData extends JobWithHistory {
    public static void doJobHistory() {
        List<OrderItems> orderItemsList = OrderItems.find("shippingInfo is not null and status<>? and createdAt >=?",
                OrderStatus.PREPARED, DateUtil.firstDayOfMonth()).fetch();

        Iterator<OrderItems> it = orderItemsList.iterator();
        OrderItems orderItems = null;
        int count = 0;
        while (it.hasNext()) {
            orderItems = it.next();
            orderItems.realGoodsPayCommission();
            Logger.info("count=%d", count);
            count++;
        }

    }
}
