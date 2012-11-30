package models.job.taobao;

import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.taobao.TaobaoCouponMessageUtil;
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
        List<OuterOrder> outerOrders = OuterOrder.find("partner = ? and extra is not null", OuterOrderPartner.TB).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            TaobaoCouponMessageUtil.send(outerOrder.id);
        }
    }
}
