package jobs.order;

import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OrderItems;
import models.order.OrderStatus;
import play.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * User: yan
 * Date: 13-6-25
 * Time: 下午5:30
 */
@JobDefine(title = "实物洗数据", description = "实物洗数据，给商户打款")
//@On("0 0 4 * * ?")
public class RealGoodsRefreshData extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<OrderItems> orderItemsList = OrderItems.find("shippingInfo is not null and status<>? and createdAt >=? and createdAt<=?",
                OrderStatus.PREPARED, DateUtil.firstDayOfMonth(), DateUtil.stringToDate("2013-06-25 23:59:59", "yyyy-MM-dd HH:mm:ss")).fetch();

        Iterator<OrderItems> it = orderItemsList.iterator();
        OrderItems orderItems = null;
        int count = 0;
        while (it.hasNext()) {
            orderItems = it.next();
            orderItems.realGoodsPayCommission();
            Logger.info("RealGoodsRefreshData 处理count=%d", count);
            count++;
        }

    }
}
