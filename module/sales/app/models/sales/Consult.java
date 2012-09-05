package models.sales;

import models.consumer.Address;
import models.consumer.CRMCondition;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrdersCondition;
import models.order.QueryType;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-4
 * Time: 下午2:18
 * To change this template use File | Settings | File Templates.
 */


public class Consult extends Model {



    public static User findUserByCondition(CRMCondition condition) {

        OrdersCondition orderCondition=new OrdersCondition();
            //查询用户信息
            User user = User.find("mobile=? or loginName=?", condition.searchUser, condition.searchUser).first();
            return user;


    }

    public static OrdersCondition findOrderCondtionByCondition(CRMCondition condition) {
        OrdersCondition orderCondition=new OrdersCondition();
        //查询用户信息
//            User user = User.find("mobile=? or loginName=?", condition.searchUser, condition.searchUser).first();
//        User user = User.find("mobile=? ", condition.searchUser).first();
//        if(user!=null)
//        {
//            orderCondition.searchKey= QueryType.MOBILE.toString();
//            orderCondition.searchItems=condition.searchUser;
//            return orderCondition;
//        }
//        user = User.find("loginName=?", condition.searchUser).first();
//        if(user!=null)
//        {
//            //"LOGIN_NAME"
//            orderCondition.searchKey=QueryType.LOGIN_NAME.toString();
//            orderCondition.searchItems=condition.searchUser;
//            return orderCondition;
//        }

        return null;
    }

    public static Address findAddressByCondition(CRMCondition condition) {
        User user = findUserByCondition(condition);
        if (user != null) {
            Address address = Address.findDefault(user);
            return address;
        }
        return null;

    }


    public static List<Order> findOrderByCondition(CRMCondition condition) {
        //  查询订单信息

            List<Order> orderList = Order.find("select distinct o from Order o, User u where " +
                    "o.userId=u.id and o.userType = models.accounts.AccountType.CONSUMER and" +
                    "(o.orderNumber=? or u.mobile=? or u.loginName=? or o.receiverMobile=? or o.buyerMobile = ?)",
                    condition.searchOrderCoupon, condition.searchUser,
                    condition.searchUser, condition.searchUser, condition.searchUser).fetch(5);
            return orderList;

    }

//    public static long findOrderByConditionSize(CRMCondition condition) {
//
//        return Order.count("select distinct o from Order o, User u where " +
//                "o.userId=u.id and o.userType = models.accounts.AccountType.CONSUMER and " +
//                "(o.orderNumber=? or u.mobile=? or u.loginName=? or o.receiverMobile=? or o.buyerMobile = ?)",
//                condition.searchOrderCoupon, condition.searchUser,
//                condition.searchUser, condition.searchUser, condition.searchUser);
//    }

//    public static long findCouponByConditionSize(CRMCondition condition) {
//        //  查询券信息       and o.userType = models.accounts.AccountType.CONSUMER
//
//        return ECoupon.count("select distinct e from ECoupon e, User u where " +
//                "e.order.userId = u.id " +
//                "and ( " +
//                "  e.eCouponSn like ? " +
//                "  or e.order.orderNumber=? " +
//                "  or u.mobile=? " +
//                "  or u.loginName=? " +
//                "  or e.order.receiverMobile=? " +
//                "  or e.order.buyerMobile = ?)",
//                "%" + condition.searchOrderCoupon, condition.searchOrderCoupon,
//                condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser);
//
//
//    }


    public static List<ECoupon> findCouponByCondition(CRMCondition condition) {
        //  查询券信息       and o.userType = models.accounts.AccountType.CONSUMER

            List<ECoupon> eCoupons = ECoupon.find("select distinct e from ECoupon e, User u where " +
                    "e.order.userId = u.id " +
                    "and ( " +
                    "  e.eCouponSn like ? " +
                    "  or e.order.orderNumber=? " +
                    "  or u.mobile=? " +
                    "  or u.loginName=? " +
                    "  or e.order.receiverMobile=? " +
                    "  or e.order.buyerMobile = ?)" +
                    "  or e.orderItems.phone = ?)"         ,
                    "%" + condition.searchOrderCoupon, condition.searchOrderCoupon,
                    condition.searchUser, condition.searchUser, condition.searchUser, condition.searchUser,condition.searchUser).fetch(5);
            return eCoupons;


    }


}
