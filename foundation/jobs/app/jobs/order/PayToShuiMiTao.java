package jobs.order;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.OrderItems;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * User: wangjia
 * Date: 13-8-5
 * Time: 下午4:56
 */
@JobDefine(title = "给水蜜桃商户打款", description = "给水蜜桃商户打款")
//@On("0 0 4 * * ?")
//@OnApplicationStart
public class PayToShuiMiTao extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<Long> goodsIds = new ArrayList<>();
        goodsIds.add(3313l);
        Integer[] tempOrderIds = {153172, 154632, 154827};
        List<Long> orderIds = new ArrayList<>();
        for (Integer orderId : tempOrderIds) {
            orderIds.add(Long.valueOf(orderId.toString()));
        }

        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT o FROM OrderItems o  WHERE o.goods.id in (:goodsId) and " +
                " o.order.id  in (:orderIds)");
        q.setParameter("goodsId", goodsIds);
        q.setParameter("orderIds", orderIds);
        List<OrderItems> orderItemsList = q.getResultList();
        for (OrderItems item : orderItemsList) {
            item.realGoodsPayCommission();
        }
    }
}
