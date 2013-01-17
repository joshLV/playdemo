package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: hejun
 * Date: 12-8-8
 * Time: 下午3:30
 */
public class PointGoodsOrderCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date applyAtBegin;
    public Date applyAtEnd;
    public Date acceptAtBegin;
    public Date acceptAtEnd;
    public Date refundAtBegin;
    public Date refundAtEnd;
    public String status;
    //    public PointGoodsOrderStatus status;
    public PointGoodsOrderSentStatus sentStatus;
    public String pointGoodsName;
    public DeliveryType deliveryType;
    public String searchKey;
    public String searchItems;
    public AccountType userType;
    public String loginName;
    public String orderNumber;


    /**
     * 查询条件hql.
     *
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

        if (userType != null) {
            sql.append(" and o.userType=:userType");
            paramsMap.put("userType", userType);
        }
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
        }
        if (acceptAtBegin != null) {
            sql.append(" and o.acceptAt >= :acceptAtBegin");
            paramsMap.put("acceptAtBegin", acceptAtBegin);
        }
        if (acceptAtEnd != null) {
            sql.append(" and o.acceptAt <= :acceptAtEnd");
            paramsMap.put("acceptAtEnd", DateUtil.getEndOfDay(acceptAtEnd));
        }
        if (refundAtBegin != null) {
            sql.append(" and o.refundAt >= :refundAtBegin");
            paramsMap.put("refundAtBegin", refundAtBegin);
        }
        if (refundAtEnd != null) {
            sql.append(" and o.refundAt <= :refundAtEnd");
            paramsMap.put("refundAtEnd", DateUtil.getEndOfDay(refundAtEnd));
        }
        if (status != null) {
            if ("APPLY".equals(status) ) {
                sql.append(" and o.status = :status");
                paramsMap.put("status", PointGoodsOrderStatus.APPLY);
            }

            if ("ACCEPT".equals(status) ) {
                sql.append(" and o.status = :status");
                paramsMap.put("status", PointGoodsOrderStatus.ACCEPT);
            }

            if ("CANCELED".equals(status) ) {
                sql.append(" and o.status = :status");
                paramsMap.put("status", PointGoodsOrderStatus.CANCELED);
            }

            if ("SENT".equals(status) ) {
                sql.append(" and o.sentStatus = :status");
                paramsMap.put("status", PointGoodsOrderSentStatus.SENT);
            }

            if ("UNSENT".equals(status)) {
                sql.append(" and o.status = :orderStatus");
                paramsMap.put("orderStatus", PointGoodsOrderStatus.ACCEPT);
                sql.append(" and o.sentStatus = :status");
                paramsMap.put("status", PointGoodsOrderSentStatus.UNSENT);
            }


        }


        if (sentStatus != null) {
            sql.append(" and o.sentStatus = :sentStatus");
            paramsMap.put("sentStatus", sentStatus);
        }


        if (deliveryType != null) {
            sql.append(" and o.deliveryType = :deliveryType");
            paramsMap.put("deliveryType", deliveryType);
        }

        //按照商品名称检索
        if (QueryType.GOODS_NAME.toString().equals(searchKey)) {
            sql.append(" and o.pointGoodsName like :name");
            paramsMap.put("name", "%" + searchItems.trim() + "%");
        }


        //商品名称
        if (StringUtils.isNotBlank(pointGoodsName)) {
            sql.append(" and o.pointGoodsName like :pointGoodsName");
            paramsMap.put("pointGoodsName", "%"+pointGoodsName.trim()+"%");
        }

        //订单号
        if (StringUtils.isNotBlank(orderNumber)) {
            sql.append(" and o.orderNumber =:orderNumber");
            paramsMap.put("orderNumber", orderNumber.trim());
        }

        //帐号
//        if (StringUtils.isNotBlank(loginName)) {
//            sql.append(" and o.user.loginName =:loginName");
//            paramsMap.put("loginName", loginName.trim());
//        }


        if (StringUtils.isNotBlank(loginName)) {
            User user = User.findByLoginName(loginName.trim());

            if (user != null) {
                sql.append(" and o.userId = :userId");
                paramsMap.put("userId", user.id);
            } else {
                sql.append(" and o.userId = :userId");
                paramsMap.put("userId", 0l);
            }

        }


        //按照帐号检索
//        if (QueryType.LOGIN_NAME.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
//            User user = User.findByLoginName(searchItems.trim());
//            if (user != null) {
//                sql.append(" and o.userId = :user");
//                paramsMap.put("user", user.getId());
//            }
//            Resaler resaler = Resaler.findOneLoginName(searchItems.trim());
//            if (resaler != null) {
//                sql.append(" and o.userId = :user");
//                paramsMap.put("user", resaler.id);
//            }
//        }


//        if (StringUtils.isNotBlank(loginName)) {
////            System.out.println("aaaa<<<bbbb");
////            User user = User.findByLoginName(loginName.trim());
////            if(user!=null)
////            {
//
////                System.out.println("aaaa<<<bbbb");
//            sql.append(" and o.user.loginName =:loginName");
//            paramsMap.put("loginName", loginName.trim() );
////        }
//        }


        //按照商品订单检索
//        if (QueryType.ORDER_NUMBER.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
//            sql.append(" and o.orderNumber like :orderNumber");
//            paramsMap.put("orderNumber", "%" + searchItems + "%");
//        }

        if (QueryType.ORDER_NUMBER.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.orderNumber like :orderNumber");
            paramsMap.put("orderNumber", searchItems);
        }


        //按照帐号检索
//        if (QueryType.LOGIN_NAME.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
//            User user = User.findByLoginName(searchItems.trim());
//            if (user != null) {
//                sql.append(" and o.userId = :user");
//                paramsMap.put("user", user.getId());
//            }
//            Resaler resaler = Resaler.findOneLoginName(searchItems.trim());
//            if (resaler != null) {
//                sql.append(" and o.userId = :user");
//                paramsMap.put("user", resaler.id);
//            }
//        }


        //按照帐号检索
//        if (QueryType.LOGIN_NAME.toString().equals(loginName) && StringUtils.isNotEmpty(loginName)) {
//            User user = User.findByLoginName(loginName.trim());
//            if (user != null) {
////                sql.append(" and o.userId = :user");
////                paramsMap.put("user", user.getId());
//
//                sql.append(" and o.user.loginName = :loginName");
//                paramsMap.put("loginName", loginName.trim());
//            }
//
//        }


        //按照手机检索
        if (QueryType.MOBILE.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.buyerMobile =:phone)");
            paramsMap.put("phone", searchItems);
        }

        return sql.toString();
    }

    /**
     * @param user 用户信息
     * @return sql 查询条件
     */
    public String getFilter(User user) {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);
        if (user != null) {
            sql.append(" and o.userId = :user and o.userType = :userType");
            paramsMap.put("user", user.getId());
            paramsMap.put("userType", AccountType.CONSUMER);
        }
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }
        //按照订单检索
        if (StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.orderNumber like :orderNumber");
            paramsMap.put("orderNumber", "%" + searchItems + "%");
        }
        //按照商品名称检索
//        if (StringUtils.isNotBlank(pointGoodsName)) {
//            sql.append(" and o.pointGoods.name like :pointGoodsName");
//            paramsMap.put("pointGoodsName", "%" + pointGoodsName + "%");
//        }

        if (StringUtils.isNotBlank(pointGoodsName)) {
            sql.append(" and o.pointGoods.name =:pointGoodsName");
            paramsMap.put("pointGoodsName", pointGoodsName.trim());
        }

        return sql.toString();
    }

    /**
     * @param resaler 用户信息
     * @return sql 查询条件
     */
    public String getResalerFilter(Resaler resaler) {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);
        if (resaler != null) {
            sql.append(" and o.userId = :resalerId and o.userType = :userType");
            paramsMap.put("resalerId", resaler.id);
            paramsMap.put("userType", AccountType.RESALER);
        }
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }

        //按照商品名称检索
        if (StringUtils.isNotBlank(pointGoodsName)) {
            sql.append(" and o.pointGoods.name like :pointGoodsName)");
            paramsMap.put("pointGoodsName", "%" + pointGoodsName + "%");
        }

        return sql.toString();
    }

    public String getOrderByExpress() {
        String orderBySql = "o.acceptAt desc,o.applyAt desc";
        return orderBySql;
    }

    public String getOrderByDate(){
        String orderBySql = "o.applyAt desc";
        return orderBySql;
    }

    public String getUserOrderByExpress() {
        String orderBySql = "o.applyAt desc";
        return orderBySql;
    }
}
