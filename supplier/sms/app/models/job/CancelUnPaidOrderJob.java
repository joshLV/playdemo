package models.job;

import com.uhuila.common.util.DateUtil;
import models.order.CancelUnpaidOrders;
import models.order.Order;
import models.order.OrderStatus;
import play.jobs.Job;
import play.jobs.On;

import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-20
 * Time: 上午11:09
 */
@On("0 0 1 * * ?")  //每天凌晨执行,自动取消过期两天的未付款的订单
public class CancelUnPaidOrderJob extends Job {

    @Override
    public void doJob() throws Exception {
        String sql = "select o from Order o where o.orderNumber not in (select c.orderNumber from CancelUnpaidOrders " +
                "c ) and o.status =:status and o.createdAt <=:createdAtEnd order by o.id";
        Query query = Order.em().createQuery(sql);
        query.setParameter("status", OrderStatus.UNPAID);
        query.setParameter("createdAtEnd", DateUtil.getEndExpiredDate(-2));
        query.setFirstResult(0);
        query.setMaxResults(200);

        Order order = null;
        List<Order> orderList = query.getResultList();
        Iterator<Order> it = orderList.iterator();
        while (it.hasNext()) {
            order = it.next();
            if (order.status == OrderStatus.UNPAID) {
                //取消订单并且增加库存和减少销量
                order.cancelAndUpdateOrder();
                new CancelUnpaidOrders(order.orderNumber, order.userType, order.userId).save();
            }
        }
    }

}
