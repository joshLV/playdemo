package models.job.resale;

import com.uhuila.common.util.DateUtil;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvRoomOrderInfo;
import models.order.Order;
import models.order.OrderStatus;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import util.DateHelper;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * 查询10分钟前下单并且未付款的订单并且关闭
 * <p/>
 * User: yan
 * Date: 13-4-16
 * Time: 下午3:56
 */
//@Every("1mn")
public class KtvOrderCancel extends Job {
    @Override
    public void doJob() {
        String sql = "select k from KtvRoomOrderInfo k where k.status=:status and k.createdAt <= :createdAt";
        Query query = KtvRoomOrderInfo.em().createQuery(sql);
        query.setParameter("status", KtvOrderStatus.LOCK);
        query.setParameter("createdAt", DateHelper.beforeMinuts(10));
        query.setFirstResult(0);
        query.setMaxResults(200);
        List<KtvRoomOrderInfo> orderInfoList = query.getResultList();
        for (KtvRoomOrderInfo orderInfo : orderInfoList) {
            orderInfo.cancelKtvRoom();
        }
    }
}
