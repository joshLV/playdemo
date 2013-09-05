package models.job.taobao;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.taobao.TaobaoCouponMessage;
import play.Logger;
import play.Play;
import play.jobs.Every;

import java.util.List;

/**
 * @author likang
 *         Date: 12-11-29
 */
@JobDefine(title = "淘宝券生成", description = "处理OuterOrder中未生成的订单，生成券并发送")
@Every("1mn")
public class TaobaoCouponJob extends JobWithHistory {
    public static final boolean ON = Play.configuration.getProperty("taobao.job", "off").toLowerCase().equals("on");

    @Override
    public void doJobWithHistory() {
        if (!ON && Play.runingInTestMode()) {
            return;
        }
        Logger.info("start taobao coupon job");
        List<OuterOrder> outerOrders = OuterOrder.find("partner = ? and (status = ? or status = ? or status = ? or status = ?)",
                OuterOrderPartner.TB,
                OuterOrderStatus.ORDER_COPY,
                OuterOrderStatus.ORDER_DONE,
                OuterOrderStatus.RESEND_COPY,
                OuterOrderStatus.REFUND_COPY).fetch();
        for (OuterOrder outerOrder : outerOrders) {
            new TaobaoCouponMessage(outerOrder.id).lockVersion(outerOrder.lockVersion).send();
        }
    }
}
