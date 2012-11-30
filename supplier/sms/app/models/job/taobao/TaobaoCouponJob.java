package models.job.taobao;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.taobao.TaobaoCouponMessageUtil;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
 import java.util.List;

/**
 * @author likang
 *         Date: 12-11-29
 */
@Every("1mn")
public class TaobaoCouponJob extends Job{
    @Override
    public void doJob() {
        Logger.info("start taobao coupon job");
        List<OuterOrder> outerOrders = OuterOrder.find("partner = ? and (status = ? or status = ? or status = ?)",
                OuterOrderPartner.TB,
                OuterOrderStatus.ORDER_COPY,
                OuterOrderStatus.ORDER_DONE,
                OuterOrderStatus.RESEND_COPY).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            TaobaoCouponMessageUtil.send(outerOrder.id);
        }
    }
}
