package models.sales;

import models.accounts.WithdrawBill;
import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-9-4
 * Time: 下午2:18
 */


public class ConsultCondition {


//    public static User findUserByCondition(CRMCondition condition) {
//
//        OrdersCondition orderCondition = new OrdersCondition();
//        //查询用户信息
//        User user = User.find("mobile=? or loginName=?", condition.searchUser, condition.searchUser).first();
//        return user;
//
//
//    }


//    public static Address findAddressByCondition(CRMCondition condition) {
//        User user = findUserByCondition(condition);
//        if (user != null) {
//            Address address = Address.findDefault(user);
//            return address;
//        }
//        return null;
//
//    }

    public static Address findAddressByCondition(User user) {

        if (user != null) {
            Address address = Address.findDefault(user);
            return address;
        }
        return null;

    }


    public static List<Order> findOrderByCondition(CRMCondition condition) {
        //  查询订单信息

        List<Order> orderList = Order.find("select distinct o from Order o, User u where " +
                "o.amount>0 and o.consumerId=u.id and" +
                "(o.orderNumber=? or u.mobile=? or u.loginName=? or o.receiverMobile=? or o.buyerMobile = ?"
                + "or o.id in (select oi.order.id from o.orderItems oi where oi.phone =?)"
                + "or u.id=?)",
                condition.searchOrderCoupon, condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser, condition.userId).fetch(5);

        return orderList;

    }


    public static long findOrderByConditionSize(CRMCondition condition) {


        return Order.count("from Order o, User u where " +
                "o.amount>0 and o.consumerId=u.id and" +
                "(o.orderNumber=? or u.mobile=? or u.loginName=? or o.receiverMobile=? or o.buyerMobile = ?"
                + "or o.id in (select oi.order.id from o.orderItems oi where oi.phone =?))",
                condition.searchOrderCoupon, condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser);
    }

    public static long findCouponByConditionSize(CRMCondition condition) {
        //  查询券信息       and o.userType = models.accounts.AccountType.CONSUMER

        return ECoupon.count("from ECoupon e, User u where " +
                "e.order.amount>0 and e.order.userId = u.id " +
                "and ( " +
                "  e.eCouponSn like ? " +
                "  or e.order.orderNumber=? " +
                "  or u.mobile=? " +
                "  or u.loginName=? " +
                "  or e.order.receiverMobile=? " +
                "  or e.order.buyerMobile = ?" +
                "  or e.orderItems.phone = ?" +
                "  or u.id=? or e.id in (select c.couponId from CouponCallBind c where c.phone=?) )",
                "%" + condition.searchOrderCoupon, condition.searchOrderCoupon,
                condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser, condition.userId, condition.searchUser);

    }


    public static List<ECoupon> findCouponByCondition(CRMCondition condition) {
        //  查询券信息       and o.userType = models.accounts.AccountType.CONSUMER

        List<ECoupon> eCoupons = ECoupon.find("select distinct e from ECoupon e, User u where " +
                "e.order.amount>0 and e.order.userId = u.id " +
                "and  e.order.userType = models.accounts.AccountType.CONSUMER and ( " +
                "  e.eCouponSn like ? " +
                "  or e.order.orderNumber=? " +
                "  or u.mobile=? " +
                "  or u.loginName=? " +
                "  or e.order.receiverMobile=? " +
                "  or e.order.buyerMobile = ?" +
                "  or e.orderItems.phone = ?" +
                "  or u.id=? or e.id in (select c.couponId from CouponCallBind c where c.phone=?) )",
                "%" + condition.searchOrderCoupon, condition.searchOrderCoupon,
                condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser, condition.userId, condition.searchUser).fetch(5);

        return eCoupons;


    }

    public static List<User> findSearchUserByCondition(CRMCondition condition) {
        List<User> searchUserList = User.find("mobile=? or loginName=? "
                + "or id in (select o.userId from Order o, ECoupon e where e.eCouponSn like ? and e.order=o)"
                + "or id in (select o.userId from Order o where o.orderNumber=?))", condition.searchUser, condition.searchUser, "%" + condition.searchOrderCoupon, condition.searchOrderCoupon
        ).fetch();


        return searchUserList;

    }

//    public static List<ECoupon> findCouponByCondition(CRMCondition condition) {
//        //  查询券信息       and o.userType = models.accounts.AccountType.CONSUMER
//
//        List<ECoupon> eCoupons = ECoupon.find("select distinct e from ECoupon e, User u, CouponCallBind c where " +
//                "e.order.userId = u.id " +
//                "and ( " +
//                "  e.eCouponSn like ? " +
//                "  or e.order.orderNumber=? " +
//                "  or u.mobile=? " +
//                "  or u.loginName=? " +
//                "  or e.order.receiverMobile=? " +
//                "  or e.order.buyerMobile = ?" +
//                "  or e.orderItems.phone = ?" +
//                "  or u.id=? or (c.couponId=e.id and c.phone=?))",
//                "%" + condition.searchOrderCoupon, condition.searchOrderCoupon,
//                condition.searchUser,
//                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser, condition.userId, condition.searchUser).fetch(5);
//
//
//        return eCoupons;
//
//
//    }


    public static List<WithdrawBill> findBillByCondition(CRMCondition condition) {
        List<WithdrawBill> withdrawBill = WithdrawBill.find(
                "account.accountType= models.accounts.AccountType.CONSUMER"
                        + " and (applier in (select loginName from User u where u.loginName=?)"
                        + "or applier in (select loginName from User u where u.mobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.receiverMobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.buyerMobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.id in (select oi.order.id from o.orderItems oi where oi.phone =?))"
                        + "or applier in (select loginName from User u where u.id=?))",
                condition.searchUser, condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser, condition.userId

        ).fetch(5);


        return withdrawBill;
    }

    public static long findBillByConditionSize(CRMCondition condition) {
        return WithdrawBill.count(
                "account.accountType= models.accounts.AccountType.CONSUMER"
                        + " and (applier in (select loginName from User u where u.loginName=?)"
                        + "or applier in (select loginName from User u where u.mobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.receiverMobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.buyerMobile=?)"
                        + "or applier in (select u.loginName from Order o, User u where o.userId=u.id and " +
                        "o.id in (select oi.order.id from o.orderItems oi where oi.phone =?)))",
                condition.searchUser, condition.searchUser,
                condition.searchUser, condition.searchUser, condition.searchUser
        );

    }


}
