package models.job;

import play.jobs.Job;

import java.text.ParseException;

/**
 * 定时取消秒杀订单.
 * <p/>
 * User: sujie
 * Date: 8/16/12
 * Time: 7:47 PM
 */
// @On("0 * * * * ?")  //每分钟执行一次,自动取消15分钟还未付款的订单
public class CancelSecKillOrderJob extends Job {

    @Override
    public void doJob() throws ParseException {
        //todo
//
//        String sql = "select o from Order o where o.orderNumber not in (select c.orderNumber from CancelUnpaidOrders " +
//                "c ) and o.status =:status and o.createdAt <=:createdAtEnd order by o.id";
//        Query query = Order.em().createQuery(sql);
//        query.setParameter("status", OrderStatus.UNPAID);
//        query.setParameter("createdAtEnd", DateUtil.getEndExpiredDate(-10));
//        query.setFirstResult(0);
//        query.setMaxResults(200);
//
//        Order order = null;
//        List<Order> orderList = query.getResultList();
//        Iterator<Order> it = orderList.iterator();
//        while (it.hasNext()) {
//            order = it.next();
//            if (order.status == OrderStatus.UNPAID) {
//                //取消订单并且增加库存和减少销量
//                order.cancelAndUpdateOrder();
//                new CancelUnpaidOrders(order.orderNumber, order.userType, order.userId).save();
//            }
//        }
    }
}
